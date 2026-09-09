---
name: "mybatis-plus-persistence"
description: "在本工作区(Spring Boot 3 + MyBatis-Plus + MySQL)新增业务模块或将内存存储模块迁移到数据库持久化时使用。包含实体注解、Mapper、Service 改造、建表脚本、级联删除、本地 MySQL 启动与端到端验证的完整标准流程。当用户要求为 blog-main-services 增加新模块、持久化功能、建表或迁移 ConcurrentHashMap 存储时调用。"
---

# MyBatis-Plus + MySQL 持久化开发规范（blog-main-services 工作区）

本工作区已完成 files / contents / comments / content_attachments 四个模块的持久化。新增模块或迁移内存存储时，**严格按本流程执行**，不要引入 JPA、MinIO 或其他持久化方案。

## 技术栈基线

- Spring Boot 3.x（**jakarta 命名空间**，如 `jakarta.annotation.Resource`），Java 17，打包 WAR
- MyBatis-Plus 3.5.x（`BaseMapper`、`@TableName`、`@TableId`、`QueryWrapper`）
- Druid 连接池 + `com.mysql.cj.jdbc.Driver`
- 启动类已有 `@MapperScan("com.blog.services.**.mapper")`，新建 Mapper 无需重复配置
- 统一响应包装：`com.blog.services.common.Result<T>`（`Result.success(data)` / `Result.badRequest(msg)` / `Result.notFound(msg)` / `Result.error(msg)`）
- 配置在 `src/main/resources/application.properties`，数据源用 `${DB_HOST:127.0.0.1}` 等占位符，**禁止硬编码数据库密码**

## 模块分层结构（必须遵守）

```
com.blog.services.<module>/
├── controllers/   XxxController.java   @RestController + @RequestMapping("/api/xxx")
├── services/      XxxService.java      接口
│                  XxxServiceImple.java @Service 实现（注意是 Imple 后缀，项目既有风格）
├── mapper/        XxxMapper.java       @Mapper + extends BaseMapper<Xxx>
└── models/
    ├── Xxx.java                      实体 @TableName
    ├── dto/        XxxDTO.java        出参
    └── vo/         CreateXxxVO.java   入参
```

## 标准实施步骤

### 1. 建表脚本（先于代码）

在 `src/main/resources/sql/schema.sql` 末尾追加 DDL，风格与现有表一致：
- 引擎 `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`
- 主键 `id BIGINT NOT NULL AUTO_INCREMENT`
- 时间字段用 `BIGINT NOT NULL COMMENT '...（毫秒时间戳）'`（项目统一用 epoch 毫秒，不用 datetime）
- 布尔/状态/类型用 `TINYINT`，计数字段 `BIGINT NOT NULL DEFAULT 0`
- 外键关联字段建普通索引（如 `KEY idx_content_id (content_id)`），多对多关联表加唯一键（如 `UNIQUE KEY uk_content_file (content_id, file_id)`）
- 长文本正文用 `MEDIUMTEXT`（不用 TEXT，64KB 不够）

### 2. 实体类

```java
@TableName("xxx")
public class Xxx implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 字段驼峰，MyBatis-Plus 自动映射下划线列名（map-underscore-to-camel-case 已开启）
    private Long createTime;
    private Long updateTime;
    // 手写 getter/setter（项目不用 Lombok）
}
```

### 3. Mapper 接口

```java
@Mapper
public interface XxxMapper extends BaseMapper<Xxx> { }
```

### 4. Service 实现要点

- 注入用 `@Resource`（`jakarta.annotation.Resource`）
- CRUD：`insert`（自增 ID 回填到实体）、`selectById`、`selectList(new QueryWrapper<Xxx>().eq(...).orderByDesc("create_time"))`、`updateById`、`deleteById`、`delete(wrapper)`
- **迁移内存存储时**：删除 `ConcurrentHashMap` + `AtomicLong`，create 时不再手动 setId，由数据库自增
- **级联删除必须加 `@Transactional(rollbackFor = Exception.class)`**：删除主记录前，先删关联表记录；若关联物理文件（附件），循环调用 `fileService.deleteFile(fileId)` 清理 files 表与磁盘文件

### 5. Controller 要点

- 路径风格：集合 `/api/xxx`，嵌套资源 `/api/contents/{contentId}/comments`
- 入参校验：ID 非空正数、必填字段 `StringUtils.isBlank`（org.apache.commons.lang3）、类型枚举值校验后返回 `Result.badRequest`
- 文件上传：`@RequestParam("file") MultipartFile file`，uploaderId 可选
- 列表筛选参数：`@RequestParam(value = "type", required = false) Integer type`

## 本地验证流程（Windows，必做）

系统 MySQL 服务因权限无法 `net start`，使用项目内已初始化的数据目录：

```powershell
# 1. 启动 MySQL（数据目录在项目下，root 空密码，库名 blog）
& "D:\Program Files\MySQL\MySQL Server 9.5\bin\mysqld.exe" `
  --datadir="D:\github\blog-main-services\mysql-data" --port=3306 --console
Start-Sleep -Seconds 8   # 等待监听 3306

# 2. 执行建表脚本
$mysql = "D:\Program Files\MySQL\MySQL Server 9.5\bin\mysql.exe"
Get-Content "D:\github\blog-main-services\src\main\resources\sql\schema.sql" | & $mysql -h 127.0.0.1 -u root blog

# 3. 编译
.\mvnw.cmd clean compile -q    # 检查 EXIT CODE = 0

# 4. 启动应用（后台，约 20 秒）
.\mvnw.cmd spring-boot:run
```

注意：
- **schema.sql 含 `DROP TABLE`，会清空开发库数据**。只想给现有库补新表时，抽取该表 DDL 段落单独管道执行，不要跑全量脚本
- **PowerShell 测试含中文的 JSON 必须用文件方式**：把请求体写入 `$env:TEMP\xxx.json`（`-Encoding utf8 -NoNewline`），curl 用 `--data-binary "@$env:TEMP\xxx.json"`；直接 `-d '...中文...'` 会因引号转义被 Spring 拒绝（框架层 400，非 Result 格式）
- 验证要同时查接口和数据库：用 `& $mysql ... -e "SELECT COUNT(*) ..."` 核对行数
- **持久化必测项**：重启应用后数据仍在（证明落库而非内存）；级联删除后关联表、files 表、uploads/ 物理文件计数均为 0
- 结束后停服：停止 spring-boot:run 后台任务 + `Stop-Process -Name mysqld -Force`

## 已知坑位

1. `jakarta.annotation.Resource` 与下载用的 `org.springframework.core.io.Resource` 同名冲突 → 注入注解用 `@Resource`，Spring Resource 在代码中用全限定名
2. 实体 Integer 对应 DB TINYINT；枚举值用常量类（参考 `ContentType`：`ARTICLE=1/VOTE=2/QUESTION=3` + 静态 `isValid()`）
3. 上传的物理文件存 `./uploads`，删除附件时必须联动删物理文件，否则磁盘泄漏
4. 回复树查询：一次性 `selectList` 出全部记录，内存中按 parentId 组装 children，避免递归查库；删除父评论要级联删子回复

## blog-main-services
provide main business services

### 环境要求
- JDK 17+
- Maven 3.8+
- MySQL 8+（本地开发可使用项目内 `mysql-data` 数据目录）

### 启动命令

#### 1. 启动 MySQL

```powershell
& "D:\Program Files\MySQL\MySQL Server 9.5\bin\mysqld.exe" `
  --datadir="D:\github\blog-main-services\mysql-data" --port=3306 --console
```

等待约 8 秒确认 3306 端口监听后，执行建表脚本（仅首次或需重建表时执行，注意会清空已有数据）：

```powershell
Get-Content "src\main\resources\sql\schema.sql" | & "D:\Program Files\MySQL\MySQL Server 9.5\bin\mysql.exe" -h 127.0.0.1 -u root blog
```

#### 2. 编译

```powershell
.\mvnw.cmd clean compile -q
```

#### 3. 启动应用

```powershell
.\mvnw.cmd spring-boot:run
```

应用默认监听 `http://localhost:8080`。

#### 4. 数据源配置

默认连接 `127.0.0.1:3306`，数据库 `blog`，用户 `root`，密码为空。
可通过环境变量覆盖：

```powershell
$env:DB_HOST="127.0.0.1"
$env:DB_PORT="3306"
$env:DB_NAME="blog"
$env:DB_USER="root"
$env:DB_PASSWORD="your_password"
```

### 测试命令

单元测试基于 JUnit 5 + Mockito，无需 MySQL 实例即可运行：

```powershell
.\mvnw.cmd test
```

测试覆盖范围：

| 测试类 | 测试数 | 覆盖内容 |
|---|---|---|
| ContentTypeTest | 6 | 类型校验（null/合法/越界） |
| ContentServiceImpleTest | 11 | 内容 CRUD + 级联删除 |
| CommentServiceImpleTest | 9 | 评论树形组装 + 级联删除子回复 |
| FileServiceImpleTest | 14 | 文件上传/删除/重命名 + 校验 |
| ContentsControllerTest | 19 | 内容 API 入参校验 + 响应码 |
| CommentsControllerTest | 10 | 评论 API 入参校验 + 响应码 |
| FilesControllerTest | 14 | 文件 API 入参校验 + 响应码 |

### API 接口概览

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/contents` | 查询内容列表（可选 `?type=1\|2\|3`） |
| POST | `/api/contents` | 创建内容 |
| GET/PUT/DELETE | `/api/contents/{id}` | 查询/更新/删除单个内容 |
| GET/POST | `/api/contents/{contentId}/comments` | 评论列表/发表评论 |
| DELETE | `/api/contents/{contentId}/comments/{id}` | 删除评论（含子回复） |
| GET/POST | `/api/contents/{contentId}/attachments` | 附件列表/上传附件 |
| DELETE | `/api/contents/{contentId}/attachments/{fileId}` | 删除附件 |
| POST | `/api/files` | 上传文件 |
| GET | `/api/files` | 文件列表 |
| GET/PUT/DELETE | `/api/files/{id}` | 查询/重命名/删除文件 |
| GET | `/api/files/{id}/download` | 下载文件 |

### 依赖
- spring-boot-starter-web
- fastjson
- commons-lang3
- mybatis-plus-boot-starter 3.5.14
- mybatis-spring-boot-starter-test
- spring-boot-starter-data-redis
- mysql-connector-j
- druid-spring-boot-3-starter
- spring-boot-starter-tomcat
- spring-boot-starter-test
- junit
- ...
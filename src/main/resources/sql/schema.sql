-- 文件管理模块建表脚本
-- 数据库: blog

CREATE DATABASE IF NOT EXISTS `blog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `blog`;

-- 文件元信息表
DROP TABLE IF EXISTS `files`;
CREATE TABLE `files` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `original_name`   VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `stored_name`     VARCHAR(255) NOT NULL COMMENT '存储后的文件名（UUID）',
    `relative_path`   VARCHAR(512) NOT NULL COMMENT '相对存储路径',
    `size`            BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    `content_type`    VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
    `extension`       VARCHAR(32)  DEFAULT NULL COMMENT '文件扩展名',
    `uploader_id`     BIGINT       DEFAULT NULL COMMENT '上传者ID',
    `create_time`     BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`     BIGINT       NOT NULL COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_uploader` (`uploader_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件元信息表';

-- 内容评论表
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `content_id`   BIGINT       NOT NULL COMMENT '内容ID',
    `author_id`    BIGINT       NOT NULL COMMENT '评论者ID',
    `content`      TEXT         NOT NULL COMMENT '评论内容',
    `parent_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '父评论ID（0表示顶级评论）',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    `update_time`  BIGINT       NOT NULL COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_content_id` (`content_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容评论表';

-- 内容附件关联表
DROP TABLE IF EXISTS `content_attachments`;
CREATE TABLE `content_attachments` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `content_id`   BIGINT       NOT NULL COMMENT '内容ID',
    `file_id`      BIGINT       NOT NULL COMMENT '文件ID（关联files表）',
    `create_time`  BIGINT       NOT NULL COMMENT '创建时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_content_file` (`content_id`, `file_id`),
    KEY `idx_content_id` (`content_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容附件关联表';

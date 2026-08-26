-- =============================================================
-- TechHub 技术社区 数据库初始化脚本
-- MySQL 8.0+
-- 执行方式:mysql -uroot -p < schema.sql  或  在 Navicat/DataGrip 里直接运行
-- =============================================================

CREATE DATABASE IF NOT EXISTS `techhub` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `techhub`;

-- ---------------------------------------------------------------
-- 1. 用户表
-- ---------------------------------------------------------------
CREATE TABLE `t_user` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`      VARCHAR(32)  NOT NULL                COMMENT '用户名(登录名)',
  `password`      VARCHAR(128) NOT NULL                COMMENT '密码(bcrypt加密,不要存明文)',
  `email`         VARCHAR(64)  DEFAULT NULL            COMMENT '邮箱',
  `phone`         VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  `avatar_url`    VARCHAR(255) DEFAULT NULL            COMMENT '头像URL',
  `bio`           VARCHAR(255) DEFAULT NULL            COMMENT '个人简介',
  `status`        TINYINT      NOT NULL DEFAULT 1      COMMENT '状态:1正常 0封禁',
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email`    (`email`),
  UNIQUE KEY `uk_phone`    (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ---------------------------------------------------------------
-- 2. 帖子表
-- ---------------------------------------------------------------
CREATE TABLE `t_post` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id`       BIGINT UNSIGNED NOT NULL                COMMENT '作者ID',
  `title`         VARCHAR(128)    NOT NULL                COMMENT '标题',
  `summary`       VARCHAR(255)    DEFAULT NULL            COMMENT '摘要(列表页展示,避免查大字段)',
  `content`       LONGTEXT        NOT NULL                COMMENT '正文(Markdown)',
  `status`        TINYINT         NOT NULL DEFAULT 1      COMMENT '1已发布 0草稿 2已删除 3待审核',
  `view_count`    INT UNSIGNED    NOT NULL DEFAULT 0      COMMENT '浏览数(冗余)',
  `like_count`    INT UNSIGNED    NOT NULL DEFAULT 0      COMMENT '点赞数(冗余)',
  `comment_count` INT UNSIGNED    NOT NULL DEFAULT 0      COMMENT '评论数(冗余)',
  `collect_count` INT UNSIGNED    NOT NULL DEFAULT 0      COMMENT '收藏数(冗余)',
  `hot_score`     BIGINT          NOT NULL DEFAULT 0      COMMENT '热度分(定时任务计算,用于排行)',
  `is_top`        TINYINT         NOT NULL DEFAULT 0      COMMENT '是否置顶',
  `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `published_at`  DATETIME        DEFAULT NULL            COMMENT '发布时间(草稿转发布时写入)',
  PRIMARY KEY (`id`),
  KEY `idx_user_published`  (`user_id`, `published_at`),
  KEY `idx_status_pubtime`  (`status`, `published_at`),
  KEY `idx_status_hot`      (`status`, `hot_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帖子表';

-- ---------------------------------------------------------------
-- 3. 评论表(多级评论,parent_id=0 为一级评论)
-- ---------------------------------------------------------------
CREATE TABLE `t_comment` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `post_id`          BIGINT UNSIGNED NOT NULL                COMMENT '所属帖子ID',
  `user_id`          BIGINT UNSIGNED NOT NULL                COMMENT '评论者ID',
  `parent_id`        BIGINT UNSIGNED NOT NULL DEFAULT 0      COMMENT '父评论ID,0表示一级评论',
  `reply_to_user_id` BIGINT UNSIGNED DEFAULT NULL            COMMENT '回复的目标用户ID(楼中楼)',
  `content`          VARCHAR(1000)   NOT NULL                COMMENT '评论内容',
  `like_count`       INT UNSIGNED    NOT NULL DEFAULT 0      COMMENT '点赞数(冗余)',
  `status`           TINYINT         NOT NULL DEFAULT 1      COMMENT '1正常 0已删除',
  `created_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_post_parent_time` (`post_id`, `parent_id`, `created_at`),
  KEY `idx_user_time`        (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评论表';

-- ---------------------------------------------------------------
-- 4. 点赞表
-- ---------------------------------------------------------------
CREATE TABLE `t_like_record` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '点赞用户ID',
  `target_type` TINYINT         NOT NULL COMMENT '目标类型:1帖子 2评论',
  `target_id`   BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '1已赞 0已取消',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='点赞记录表';

-- ---------------------------------------------------------------
-- 5. 收藏表
-- ---------------------------------------------------------------
CREATE TABLE `t_collect_record` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '收藏用户ID',
  `post_id`     BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '1已收藏 0已取消',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_post` (`user_id`, `post_id`),
  KEY `idx_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收藏记录表';

-- ---------------------------------------------------------------
-- 6. 关注表
-- ---------------------------------------------------------------
CREATE TABLE `t_follow` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '关注者ID(follower)',
  `followee_id` BIGINT UNSIGNED NOT NULL COMMENT '被关注者ID',
  `status`      TINYINT         NOT NULL DEFAULT 1 COMMENT '1已关注 0已取关',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_followee` (`user_id`, `followee_id`),
  KEY `idx_followee_time` (`followee_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='关注关系表';

-- ---------------------------------------------------------------
-- 7. 标签表
-- ---------------------------------------------------------------
CREATE TABLE `t_tag` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(32) NOT NULL COMMENT '标签名',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '标签描述',
  `post_count`  INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '该标签下帖子数(冗余)',
  `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='标签表';

-- ---------------------------------------------------------------
-- 8. 帖子-标签关联表(多对多,复合主键)
-- ---------------------------------------------------------------
CREATE TABLE `t_post_tag` (
  `post_id` BIGINT UNSIGNED NOT NULL,
  `tag_id`  BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`post_id`, `tag_id`),
  KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帖子标签关联表';

-- ---------------------------------------------------------------
-- 9. 通知表
-- ---------------------------------------------------------------
CREATE TABLE `t_notification` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `sender_id`   BIGINT UNSIGNED NOT NULL COMMENT '触发者ID',
  `type`        TINYINT         NOT NULL COMMENT '1点赞 2评论 3关注 4系统通知',
  `target_type` TINYINT         NOT NULL COMMENT '目标类型:1帖子 2评论',
  `target_id`   BIGINT UNSIGNED NOT NULL COMMENT '目标ID(点击跳转用)',
  `content`     VARCHAR(255)    NOT NULL COMMENT '通知内容(冗余快照,避免回查)',
  `is_read`     TINYINT         NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
  `created_at`  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read_time` (`user_id`, `is_read`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通知表';

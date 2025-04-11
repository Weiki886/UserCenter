-- 添加用户封禁相关字段
ALTER TABLE `user`
    ADD COLUMN `is_banned` tinyint NOT NULL DEFAULT '0' COMMENT '是否封禁 0 - 否 1 - 是',
    ADD COLUMN `unban_date` datetime DEFAULT NULL COMMENT '解封日期，null表示永久封禁',
    ADD COLUMN `ban_reason` varchar(512) DEFAULT NULL COMMENT '封禁原因'; 
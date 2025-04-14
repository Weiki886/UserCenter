-- 删除原有的唯一索引
ALTER TABLE user DROP INDEX uni_userAccount;

-- 创建新的条件唯一索引，使其只对未删除的用户生效
-- 对于MySQL 8.0+，可以使用以下语句
ALTER TABLE user ADD CONSTRAINT uni_userAccount UNIQUE (user_account, is_delete);

-- 注意：执行此脚本前请确保备份数据库
-- 此修改允许相同账号存在多条记录，但要求(user_account, is_delete)组合唯一
-- 这意味着同一账号可以有一条is_delete=0和一条is_delete=1的记录 
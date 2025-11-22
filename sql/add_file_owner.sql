-- 为文件表添加上传者字段
ALTER TABLE lfs_file 
ADD COLUMN user_id bigint(20) DEFAULT 0 COMMENT '上传者用户ID' AFTER in_trash,
ADD COLUMN username varchar(50) DEFAULT '' COMMENT '上传者用户名' AFTER user_id,
ADD KEY idx_user_id (user_id);



-- 文件操作记录表
CREATE TABLE IF NOT EXISTS lfs_file_operation_log
(
    id            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    user_id       bigint(20) NOT NULL COMMENT '用户ID',
    username      varchar(50)  DEFAULT '' COMMENT '用户名',
    operation     varchar(20)  DEFAULT '' COMMENT '操作类型：UPLOAD上传, DOWNLOAD下载, MOVE移动',
    file_id       bigint(20)   DEFAULT 0 COMMENT '文件ID',
    file_name     varchar(255) DEFAULT '' COMMENT '文件名',
    file_path     varchar(500) DEFAULT '' COMMENT '文件路径',
    target_path   varchar(500) DEFAULT '' COMMENT '目标路径（移动操作）',
    file_size     bigint(20)   DEFAULT 0 COMMENT '文件大小',
    ip_address    varchar(50)  DEFAULT '' COMMENT 'IP地址',
    deleted       tinyint(1)   DEFAULT 0 COMMENT '是否删除，0未删除，1删除',
    create_time   datetime(3)  DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
    update_time   datetime(3)  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_operation (operation),
    KEY idx_create_time (create_time),
    KEY idx_file_id (file_id)
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARSET = utf8mb4 COMMENT = '文件操作记录表';


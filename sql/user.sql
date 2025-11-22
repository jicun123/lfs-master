-- 用户表
create table if not exists lfs_user
(
    id          bigint(20) not null auto_increment comment '用户id',
    username    varchar(50)  not null comment '用户名',
    password    varchar(255) not null comment '密码（加密）',
    nickname    varchar(50)  default '' comment '昵称',
    role        tinyint(1)   default '1' comment '角色，0管理员，1普通用户',
    status      tinyint(1)   default '1' comment '状态，0禁用，1启用',
    deleted     tinyint(1)   default 0 comment '是否删除，0未删除，1删除',
    create_time datetime(3)  default current_timestamp(3) comment '创建时间',
    update_time datetime(3)  default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
    primary key (id),
    unique key uk_username (username)
) engine = innodb auto_increment = 1 charset = utf8mb4 comment = '用户表';

-- 插入默认管理员账号（用户名：admin，密码：admin123）
-- 使用标准BCrypt加密 ($2a$10$表示BCrypt算法，10轮加密)
INSERT INTO lfs_user (username, password, nickname, role, status) 
VALUES ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin', 0, 1);

-- 插入默认普通用户账号（用户名：user，密码：user123）
INSERT INTO lfs_user (username, password, nickname, role, status) 
VALUES ('user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'User', 1, 1);


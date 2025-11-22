ALTER TABLE lfs_file ADD COLUMN in_trash tinyint(1) DEFAULT '0' COMMENT '是否在回收站，0表示未进入回收站，1表示进入回收站';

create table if not exists lfs_file_trash
(
    id           bigint(20) not null auto_increment comment '回收站id',
    file_id      bigint(20)  default '0' comment '原文件id',
    retain_days  int(11)     default '0' comment '保留天数',
    expire_time  bigint(20)  default '0' comment '过期时间，过期后删除文件',
    recycle_time bigint(20)  default '0' comment '回收时间，过期前回收文件',
    deleted      tinyint(1)  default 0 comment '是否删除，0未删除，1删除',
    create_time  datetime(3) default current_timestamp(3) comment '创建时间',
    update_time  datetime(3) default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
    primary key (id),
    key idx_file_id (file_id)
) engine = innodb auto_increment = 1 comment = '文件回收站记录';

create table if not exists lfs_file_trash_detail
(
    id           bigint(20) not null auto_increment comment '回收站明细id',
    trash_id     bigint(20)  default '0' comment '回收站id',
    file_id      bigint(20)  default '0' comment '原文件id',
    deleted      tinyint(1)  default 0 comment '是否删除，0未删除，1删除',
    create_time  datetime(3) default current_timestamp(3) comment '创建时间',
    update_time  datetime(3) default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
    primary key (id),
    key idx_trash_id (trash_id)
) engine = innodb auto_increment = 1 comment = '文件回收站明细记录';

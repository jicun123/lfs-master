create table if not exists lfs_file
(
    id           bigint(20) not null auto_increment comment '文件id',
    name         varchar(255) default '' comment '文件名',
    is_dir       bigint(1)    DEFAULT '0' comment '是否文件夹，0是文件，1是文件夹',
    dir_id       bigint(20)   DEFAULT '0' comment '文件夹id',
    file_size    bigint(20)   default '0' comment '文件大小，单位B',
    file_type    tinyint(1)   default '0' comment '文件类型，0文件夹，1视频，2音频，3文档，4图片，9其他',
    md5          varchar(32)  default '' comment '文件MD5',
    suffix       varchar(32)  default '' comment '文件后缀',
    duration     bigint(20)   default '0' comment '音视频时长（秒）',
    pages        int(11)      default '0' comment '文档页数',
    path         varchar(255) default '' comment '文件相对路径',
    thum_path    varchar(255) default '' comment '文件缩略图相对路径',
    trans_status tinyint(1)   default '0' comment '文件转码状态，0 正在转码，1 转码成功，2 部分转码成功，3 转码失败，4 不需要转码不需要转码，5 不支持转码，6 取消转码',
    in_trash     tinyint(1)   DEFAULT '0' comment '是否在回收站，0表示未进入回收站，1表示进入回收站',
    deleted      tinyint(1)   default 0 comment '是否删除，0未删除，1删除',
    create_time  datetime(3)  default CURRENT_TIMESTAMP(3) comment '创建时间',
    update_time  datetime(3)  default CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) comment '更新时间',
    primary key (id),
    key idx_md5 (md5)
) engine = innodb auto_increment = 1 comment = '文件表';

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


create table if not exists lfs_trans_file
(
    id          bigint(20) not null auto_increment comment '转码文件id',
    file_id     bigint(20)   default '0' comment '原文件id',
    file_size   bigint(20)   default '0' comment '文件大小，单位b',
    md5         varchar(32)  default '' comment '文件md5',
    suffix      varchar(32)  default '' comment '文件后缀',
    duration    bigint(20)   default '0' comment '音视频时长（秒）',
    pages       int(11)      default '0' comment '文档页数',
    path        varchar(255) default '' comment '文件相对路径',
    deleted     tinyint(1)   default 0 comment '是否删除，0未删除，1删除',
    create_time datetime(3)  default current_timestamp(3) comment '创建时间',
    update_time datetime(3)  default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
    primary key (id),
    key idx_file_id (file_id)
) engine = innodb auto_increment = 1 comment = '转码文件表';

create table if not exists lfs_file_thum
(
    id          bigint(20) not null auto_increment comment '转码文件id',
    file_md5    varchar(32)  default '' comment '文件md5',
    path        varchar(255) default '' comment '文件相对路径',
    duration    bigint(20)   default '0' comment '音视频所在时长（秒）',
    pages       int(11)      default '0' comment '文档所在页数',
    deleted     tinyint(1)   default 0 comment '是否删除，0未删除，1删除',
    create_time datetime(3)  default current_timestamp(3) comment '创建时间',
    update_time datetime(3)  default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
    primary key (id),
    key idx_file_md5 (file_md5)
) engine = innodb auto_increment = 1 comment = '转码文件表';

create table if not exists lfs_trans_template
(
    id                bigint(20) not null auto_increment comment '转码模板id',
    name              varchar(255) default '' comment '模板名称',
    width             int(11)      default '0' comment '视频分辨率宽度，0为自动计算',
    height            int(11)      default '1080' comment '视频分辨率高度，0为自动计算',
    format            varchar(4)   default 'mp4' comment '转码输出格式',
    frame_rate        int(11)      default '30' comment '视频帧率',
    bit_rate          int(11)      default '1200' comment '视频比特率(kbps)',
    codec             varchar(10)  default 'h264' comment '编解码器',
    audio_codec       varchar(10)  default 'aac' comment '音频编解码器',
    audio_channel     tinyint(1)   default '2' comment '音频声道',
    audio_bit_rate    int(11)      default '128' comment '音频比特率(kbps)',
    audio_sample_rate int(11)      default '48000' comment '音频采样率',
    status            tinyint(1)   default '0' comment '状态，是否开启转码，0 关闭，1 开启',
    deleted           tinyint(1)   default 0 comment '是否删除，0未删除，1删除',
    create_time       datetime(3)  default current_timestamp(3) comment '创建时间',
    update_time       datetime(3)  default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
    primary key (id)
) engine = innodb auto_increment = 1 comment = '转码模板表';


create table if not exists lfs_trans_progress
(
    id            bigint(20) not null auto_increment comment '转码进度id',
    file_id       bigint(20)    default '0' comment '转码的文件id',
    file_trans_id bigint(20)    default '0' comment '转码后的文件id',
    format        varchar(4)    default 'mp4' comment '转码格式',
    progress      double(11, 1) default '0.0' comment '转码进度，0-100(%)',
    trans_status  tinyint(1)    default '0' comment '文件转码状态，0 正在转码，1 转码成功，2 部分转码成功，3 转码失败，4 不需要转码，5 不支持转码，6 取消转码',
    start_time    bigint(20)    default '0' comment '转码开始时间（时间戳）',
    end_time      bigint(20)    default '0' comment '转码结束时间（时间戳）',
    message       varchar(255)  default '' comment '转码信息，如异常消息',
    deleted       tinyint(1)    default 0 comment '是否删除，0未删除，1删除',
    create_time   datetime(3)   default current_timestamp(3) comment '创建时间',
    update_time   datetime(3)   default current_timestamp(3) on update current_timestamp(3) comment '更新时间',
    primary key (id),
    key idx_file_id (file_id)
) engine = innodb auto_increment = 1 comment = '转码进度表';

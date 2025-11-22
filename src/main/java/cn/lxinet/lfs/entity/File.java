package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件
 *
 * @author zcx
 * @date 2023/11/09
 */
@Data
@TableName("lfs_file")
@JsonIgnoreProperties({"updateTime", "deleted"})
public class File implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Integer isDir;
    private Long dirId;
    private Long fileSize;
    private Integer fileType;
    private String suffix;
    private String md5;
    private Long duration;
    private String path;
    private Integer transStatus;
    private Integer inTrash;
    private Long userId;
    private String username;
    private String thumPath;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public File(){

    }

    public File(String name, String suffix, String path, Integer transStatus){
        this.name = name;
        this.suffix = suffix;
        this.path = path;
        this.transStatus = transStatus;
    }

    public File(Long dirId, String md5, String name, Long fileSize, String suffix, String path, Long duration,Integer transStatus, Integer fileType){
        this.dirId = dirId;
        this.md5 = md5;
        this.name = name;
        this.fileSize = fileSize;
        this.suffix = suffix;
        this.path = path;
        this.duration = duration;
        this.transStatus = transStatus;
        this.fileType = fileType;
    }

}

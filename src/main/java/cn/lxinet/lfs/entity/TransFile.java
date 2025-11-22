package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 转码文件
 *
 * @author zcx
 * @date 2023/12/15
 */
@Data
@TableName("lfs_trans_file")
public class TransFile implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long fileId;
    private Long fileSize;
    private String suffix;
    private Long duration;
    private Integer pages;
    private String md5;
    private String path;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public TransFile(){

    }

    public TransFile(Long fileId, Long fileSize, String suffix, String path){
        this.fileId = fileId;
        this.fileSize = fileSize;
        this.suffix = suffix;
        this.path = path;
    }

    public TransFile(Long fileId, String md5, Long fileSize, String suffix, String path){
        this.fileId = fileId;
        this.md5 = md5;
        this.fileSize = fileSize;
        this.suffix = suffix;
        this.path = path;
    }

}

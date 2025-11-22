package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件回收站
 *
 * @author zcx
 * @date 2023/11/30
 */
@Data
@TableName("lfs_file_trash")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileTrash implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long fileId;
    private Integer retainDays;
    private Long expireTime;
    private Long recycleTime;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public FileTrash(){

    }

    public FileTrash(Long fileId, Integer retainDays, Long expireTime, Long recycleTime){
        this.fileId = fileId;
        this.retainDays = retainDays;
        this.expireTime = expireTime;
        this.recycleTime = recycleTime;
    }

}

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
@TableName("lfs_file_trash_detail")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileTrashDetail implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long trashId;
    private Long fileId;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public FileTrashDetail(){

    }

    public FileTrashDetail(Long trashId, Long fileId){
        this.trashId = trashId;
        this.fileId = fileId;
    }

}

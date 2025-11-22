package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件缩略图
 *
 * @author zcx
 * @date 2023/11/25
 */
@Data
@TableName("lfs_file_thum")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileThum implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String fileMd5;
    private String path;
    private Long duration;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public FileThum(){

    }

    public FileThum(String fileMd5, String path, Long duration){
        this.fileMd5 = fileMd5;
        this.path = path;
        this.duration = duration;
    }

}

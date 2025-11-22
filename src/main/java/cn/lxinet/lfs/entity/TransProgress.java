package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 转码进度
 *
 * @author zcx
 * @date 2023/11/27
 */
@Data
@TableName("lfs_trans_progress")
@JsonIgnoreProperties({"updateTime"})
public class TransProgress implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private Long fileId;
    private Long fileTransId;
    private Double progress;
    private String format;
    private Integer transStatus;
    private Long startTime;
    private Long endTime;
    private String message;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public TransProgress(){

    }

    public TransProgress(Long fileId, String format){
        this.fileId = fileId;
        this.format = format;
    }

    public TransProgress(Long fileId, String format, Double progress, Long startTime){
        this.fileId = fileId;
        this.format = format;
        this.progress = progress;
        this.startTime = startTime;
    }

    public TransProgress(Long fileId, String format, Double progress, Long fileTransId, Integer transStatus, Long startTime, Long endTime){
        this.fileId = fileId;
        this.format = format;
        this.progress = progress;
        this.fileTransId = fileTransId;
        this.transStatus = transStatus;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}

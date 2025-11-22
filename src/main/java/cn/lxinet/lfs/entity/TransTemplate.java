package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 转码模板
 *
 * @author zcx
 * @date 2023/11/26
 */
@Data
@TableName("lfs_trans_template")
@JsonIgnoreProperties({"updateTime"})
public class TransTemplate implements Serializable {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private Integer width;
    private Integer height;
    private String format;
    private Integer frameRate;
    private Integer bitRate;
    private String codec;
    private String audioCodec;
    private Integer audioChannel;
    private Integer audioBitRate;
    private Integer audioSampleRate;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public TransTemplate(){

    }

}

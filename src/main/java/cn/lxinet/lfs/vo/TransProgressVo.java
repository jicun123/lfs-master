package cn.lxinet.lfs.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件转码进度
 *
 * @author zcx
 * @date 2023/11/25
 */
@Data
public class TransProgressVo implements Serializable {
    private Long id;
    private Long fileId;
    private Long fileTransId;
    private Double progress;
    private String format;
    private Integer transStatus;
    private Long startTime;
    private Long endTime;
    private String message;
    private String fileName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String previewUrl;
    private Long fileSize;

}

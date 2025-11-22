package cn.lxinet.lfs.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 文件
 *
 * @author zcx
 * @date 2023/11/25
 */
@Data
public class FileVo implements Serializable {
    private Long id;
    private String name;
    private Integer isDir;
    private Long dirId;
    private String dirName;
    private Long fileSize;
    private String suffix;
    private String md5;
    private Long duration;
    @JsonIgnore
    private String path;
    private Integer transStatus;
    @JsonIgnore
    private String thumPath;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private String fileUrl;
    private String previewUrl;
    private List<String > fileTransUrls;
    private List<TransProgressVo> progressList;
    private String thumUrl;
    //1视频，2文档
    private Integer fileType;
    private String pdfWatermark;

}

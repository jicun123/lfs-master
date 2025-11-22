package cn.lxinet.lfs.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件回收站
 *
 * @author zcx
 * @date 2024/03/16
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileTrashVo implements Serializable {
    private Long id;
    private Long fileId;
    private String fileName;
    private Long fileSize;
    private Integer isDir;
    private Integer fileType;
    private Integer retainDays;
    private Long expireTime;
    private Long recycleTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    public FileTrashVo(){

    }

}

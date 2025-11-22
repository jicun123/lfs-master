package cn.lxinet.lfs.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * 文件缩略图
 *
 * @author zcx
 * @date 2023/11/25
 */
@Data
public class FileThumVo implements Serializable {
    private Long id;
    @JsonIgnore
    private String path;
    private String fileUrl;
    private Long duration;
    
}

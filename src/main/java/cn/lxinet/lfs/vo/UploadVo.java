package cn.lxinet.lfs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件上传返回类
 *
 * @author zcx
 * @date 2023/11/20
 */
@Data
public class UploadVo implements Serializable {

    private String uploadId;

    private boolean skip;

    private Long fileId;

    public UploadVo(){

    }

    public UploadVo(String uploadId){
        this.uploadId = uploadId;
        this.skip = false;
        this.fileId = 0L;
    }

    public UploadVo(boolean skip, Long fileId){
        this.uploadId = "";
        this.skip = skip;
        this.fileId = fileId;
    }

}

package cn.lxinet.lfs.config;

import cn.lxinet.lfs.service.SafetychainService;
import cn.lxinet.lfs.utils.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 文件配置
 *
 * @author zcx
 * @date 2023/11/22
 */
@Component
public class FileConfig {
    private static final String SEPARATOR = java.io.File.separator;
    @Value("${config.file-server.type}")
    private String fileServerType;
    @Value("${config.file-server.local.file-dir}")
    private String localFileDir;
    @Value("${config.file-server.local.preview-url}")
    private String localPreviewUrl;
    @Autowired
    private SafetychainService safetychainService;

    /**
     * 上传id redis key
     * @param uploadId
     * @return
     */
    public String getUploadIdKey(String uploadId){
        return "uploadId:" + uploadId;
    }

    public String getUploadFileDir(){
        return FileUtil.mkDirs(localFileDir + SEPARATOR + "files");
    }

    public String getUploadFilePath(String suffix){
        return getUploadFileDir() + SEPARATOR + UUID.randomUUID() + suffix;
    }

    public String getVideoTransDir(){
        return FileUtil.mkDirs(localFileDir + SEPARATOR + "trans" + SEPARATOR + "video");
    }

    public String getDocumentTransDir(){
        return FileUtil.mkDirs(localFileDir + SEPARATOR + "trans" + SEPARATOR + "document");
    }

    public String getVideoTransPath(){
        return getVideoTransDir() + SEPARATOR + UUID.randomUUID() + ".mp4";
    }

    public String getDocumentTransPath(){
        return getDocumentTransDir() + SEPARATOR + UUID.randomUUID() + ".pdf";
    }

    public String getVideoHlsDir(String uuid){
        return FileUtil.mkDirs(localFileDir + SEPARATOR + "trans" + SEPARATOR + "hls" + SEPARATOR + uuid);
    }

    public String getVideoHlsPath(String uuid){
        return getVideoHlsDir(uuid) + SEPARATOR + "video.m3u8";
    }

    public String getUploadTempDir(String uploadId){
        return FileUtil.mkDirs(localFileDir + SEPARATOR + "temp" + SEPARATOR + uploadId);
    }

    public String getThumDir(String md5){
        return FileUtil.mkDirs(localFileDir + SEPARATOR + "thum" + SEPARATOR + md5);
    }

    public String getThumPath(String md5, Long index){
        return getThumDir(md5) + SEPARATOR + index + ".jpg";
    }

    public java.io.File getTempChunkFile(String uploadId, Integer chunkNumber){
        return new java.io.File(getUploadTempDir(uploadId) + SEPARATOR + chunkNumber);
    }

    public Integer getLocalTempChunkNum(String uploadId){
        java.io.File file = new java.io.File(getUploadTempDir(uploadId));
        FileUtil.mkDirs(file);
        return file.list().length;
    }

    public String getLocalFileDir() {
        return localFileDir;
    }

    public String getFileServerType() {
        return fileServerType;
    }

    public String getPreviewUrl(String path) {
        if (StringUtils.isBlank(path)){
            return "";
        }
        return localPreviewUrl + safetychainService.getEncryUrl(path);
    }

    public String getDownloadUrl(String fileName, String path) {
        if (StringUtils.isBlank(path)){
            return "";
        }
        return localPreviewUrl + safetychainService.getEncryUrl(path) + "&oper=down&filename=" + fileName;
    }
}

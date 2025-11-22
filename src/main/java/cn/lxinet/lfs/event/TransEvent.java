package cn.lxinet.lfs.event;

import cn.lxinet.lfs.enums.EventTransType;
import cn.lxinet.lfs.enums.FileType;
import org.springframework.context.ApplicationEvent;

/**
 * 转码事件
 *
 * @author zcx
 * @date 2023/11/21
 */
public class TransEvent extends ApplicationEvent {
    /**
     * 类型，0转码，1转hls
     */
    private EventTransType type;
    /**
     * 文件类型，0视频，1文档
     */
    private FileType fileType;
    /**
     * 文件id
     */
    private Long fileId;
    /**
     * 原文件绝对路径
     */
    private String filePath;
    /**
     * 文件md5
     */
    private String md5;
    /**
     * 视频总时长
     */
    private Long duration;

    public TransEvent(Object source, Long fileId, EventTransType type, FileType fileType, String filePath) {
        super(source);
        this.fileId = fileId;
        this.type = type;
        this.fileType = fileType;
        this.filePath = filePath;
    }

    public TransEvent(Object source, Long fileId, EventTransType type, FileType fileType, String filePath, String md5) {
        super(source);
        this.fileId = fileId;
        this.type = type;
        this.fileType = fileType;
        this.filePath = filePath;
        this.md5 = md5;
    }

    public TransEvent(Object source, Long fileId, EventTransType type, FileType fileType, String filePath, String md5, Long duration) {
        super(source);
        this.fileId = fileId;
        this.type = type;
        this.fileType = fileType;
        this.filePath = filePath;
        this.md5 = md5;
        this.duration = duration;
    }

    public EventTransType getType() {
        return type;
    }

    public void setType(EventTransType type) {
        this.type = type;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }
}

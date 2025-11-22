package cn.lxinet.lfs.event;

import org.springframework.context.ApplicationEvent;

/**
 * 文件MD5计算事件
 *
 * @author zcx
 * @date 2023/11/20
 */
public class FileMd5Event extends ApplicationEvent {
    private Long fileId;
    private Long fileTransId;
    private String filePath;

    public FileMd5Event(Object source, String filePath) {
        super(source);
        this.filePath = filePath;
    }

    public FileMd5Event(Object source, String filePath, Long fileId, Long fileTransId) {
        super(source);
        this.filePath = filePath;
        this.fileId = fileId;
        this.fileTransId = fileTransId;
    }

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getFileTransId() {
        return fileTransId;
    }

    public void setFileTransId(Long fileTransId) {
        this.fileTransId = fileTransId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}

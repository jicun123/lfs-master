package cn.lxinet.lfs.event;

import cn.lxinet.lfs.enums.EventTransType;
import cn.lxinet.lfs.enums.FileTransStatus;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 转码完成事件
 *
 * @author zcx
 * @date 2023/11/22
 */
public class TransCompleteEvent extends ApplicationEvent {
    /**
     * 类型，0转码，1转hls
     */
    private EventTransType type;
    /**
     * 文件id
     */
    private Long fileId;
    /**
     * 转码进度id
     */
    private Long transProgressId;
    /**
     * 转码后的文件绝对路径
     */
    private String filePath;
    /**
     * 转码后的mp4文件绝对路径，如果是转m3u8，需要先转mp4，根据mp4文件来计算大小
     */
    private String mp4FilePath;
    /**
     * 转码状态
     */
    private FileTransStatus transStatus;

    private String md5;

    private List<String> thumList;

    private List<Long> secondList;

    private String message;

    public TransCompleteEvent(Object source, Long fileId, Long transProgressId, EventTransType type) {
        super(source);
        this.fileId = fileId;
        this.transProgressId = transProgressId;
        this.type = type;
    }

    public TransCompleteEvent(Object source, Long fileId, Long transProgressId, EventTransType type, FileTransStatus transStatus) {
        super(source);
        this.fileId = fileId;
        this.transProgressId = transProgressId;
        this.type = type;
        this.transStatus = transStatus;
    }

    public TransCompleteEvent(Object source, Long fileId, String md5, List<String> thumList, List<Long> secondList, EventTransType type) {
        super(source);
        this.fileId = fileId;
        this.md5 = md5;
        this.thumList = thumList;
        this.secondList = secondList;
        this.type = type;
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

    public FileTransStatus getTransStatus() {
        return transStatus;
    }

    public void setTransStatus(FileTransStatus transStatus) {
        this.transStatus = transStatus;
    }

    public EventTransType getType() {
        return type;
    }

    public void setType(EventTransType type) {
        this.type = type;
    }

    public String getMp4FilePath() {
        return mp4FilePath;
    }

    public void setMp4FilePath(String mp4FilePath) {
        this.mp4FilePath = mp4FilePath;
    }

    public List<String> getThumList() {
        return thumList;
    }

    public void setThumList(List<String> thumList) {
        this.thumList = thumList;
    }

    public List<Long> getSecondList() {
        return secondList;
    }

    public void setSecondList(List<Long> secondList) {
        this.secondList = secondList;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Long getTransProgressId() {
        return transProgressId;
    }

    public void setTransProgressId(Long transProgressId) {
        this.transProgressId = transProgressId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package cn.lxinet.lfs.event;

import org.springframework.context.ApplicationEvent;

/**
 * 转码进度
 *
 * @author zcx
 * @date 2023/11/27
 */
public class TransProgressEvent extends ApplicationEvent {
    /**
     * 转码进度id
     */
    private Long transProgressId;
    /**
     * 转码后的文件id
     */
    private Long fileTransId;
    /**
     * 转码进度
     */
    private Double progress;
    /**
     * 转码状态
     */
    private Integer transStatus;

    private boolean start;


    public TransProgressEvent(Object source, Long transProgressId, Double progress, boolean start) {
        super(source);
        this.transProgressId = transProgressId;
        this.progress = progress;
        this.start = start;
    }

    public TransProgressEvent(Object source, Long transProgressId, Integer transStatus) {
        super(source);
        this.transProgressId = transProgressId;
        this.transStatus = transStatus;
    }

    public TransProgressEvent(Object source, Long transProgressId, Double progress, Long fileTransId, Integer transStatus) {
        super(source);
        this.transProgressId = transProgressId;
        this.progress = progress;
        this.fileTransId = fileTransId;
        this.transStatus = transStatus;
    }

    public Long getTransProgressId() {
        return transProgressId;
    }

    public void setTransProgressId(Long transProgressId) {
        this.transProgressId = transProgressId;
    }

    public Long getFileTransId() {
        return fileTransId;
    }

    public void setFileTransId(Long fileTransId) {
        this.fileTransId = fileTransId;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Integer getTransStatus() {
        return transStatus;
    }

    public void setTransStatus(Integer transStatus) {
        this.transStatus = transStatus;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean start) {
        this.start = start;
    }
}

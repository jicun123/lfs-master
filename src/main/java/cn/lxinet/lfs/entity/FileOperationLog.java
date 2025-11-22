package cn.lxinet.lfs.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 文件操作记录实体
 *
 * @author system
 * @date 2025/11/04
 */
@Data
@TableName("lfs_file_operation_log")
public class FileOperationLog implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 操作类型：UPLOAD上传, DOWNLOAD下载, MOVE移动
     */
    private String operation;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 目标路径（移动操作）
     */
    private String targetPath;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 是否删除，0未删除，1删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 操作时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    public FileOperationLog() {
    }

    public FileOperationLog(Long userId, String username, String operation, Long fileId, 
                            String fileName, String filePath, Long fileSize, String ipAddress) {
        this.userId = userId;
        this.username = username;
        this.operation = operation;
        this.fileId = fileId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.ipAddress = ipAddress;
    }
}


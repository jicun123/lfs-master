package cn.lxinet.lfs.enums;

/**
 * 文件操作类型枚举
 *
 * @author system
 * @date 2025/11/04
 */
public enum OperationType {
    /**
     * 上传
     */
    UPLOAD("UPLOAD", "上传"),
    
    /**
     * 下载
     */
    DOWNLOAD("DOWNLOAD", "下载"),
    
    /**
     * 移动
     */
    MOVE("MOVE", "移动");

    private final String code;
    private final String desc;

    OperationType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}


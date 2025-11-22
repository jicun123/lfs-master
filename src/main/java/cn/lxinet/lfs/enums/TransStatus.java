package cn.lxinet.lfs.enums;

/**
 * 文件转码状态
 *
 * @author zcx
 * @date 2023/12/10
 */
public enum TransStatus {
    /**
     * 正在转码
     */
    TRANS(0),
    /**
     * 转码成功
     */
    TRANS_SUCCESS(1),
    /**
     * 部分转码成功
     */
    PART_TRANS_SUCCESS(2),
    /**
     * 转码失败
     */
    TRANS_FAIL(3),
    /**
     * 不需要转码
     */
    NO_NEED_TRANS(4),
    /**
     * 不支持转码
     */
    NO_SUPPORT_TRANS(5),
    /**
     * 转码取消
     */
    TRANS_CANCEL(6);

    private Integer status;

    TransStatus(Integer status){
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}

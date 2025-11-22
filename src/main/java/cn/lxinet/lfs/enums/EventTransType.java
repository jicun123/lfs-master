package cn.lxinet.lfs.enums;

/**
 * 转码类型
 *
 * @author zcx
 * @date 2023/11/20
 */
public enum EventTransType {
    TO_MP4(0),
    TO_HLS(1),
    TO_PDF(2),
    GEN_VIDEO(3),
    GEN_PDF(4),
    ;

    private Integer type;

    EventTransType(Integer type){
        this.type = type;
    }

    public Integer getType() {
        return type;
    }
}

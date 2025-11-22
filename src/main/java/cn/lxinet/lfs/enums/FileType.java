package cn.lxinet.lfs.enums;

/**
 * 文件类型
 *
 * @author zcx
 * @date 2023/11/20
 */
public enum FileType {
    DIR(0),
    VIDEO(1),
    AUDIO(2),
    DOCUMENT(3),
    IMAGE(4),
    OTHER(9),
    ;

    private Integer value;

    FileType(Integer value){
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}

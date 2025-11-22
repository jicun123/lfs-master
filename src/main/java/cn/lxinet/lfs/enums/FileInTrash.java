package cn.lxinet.lfs.enums;

/**
 * 文件是否在回收站
 *
 * @author zcx
 * @date 2024/03/20
 */
public enum FileInTrash {

    NO(0),

    YES(1);

    private Integer value;

    FileInTrash(Integer value){
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}

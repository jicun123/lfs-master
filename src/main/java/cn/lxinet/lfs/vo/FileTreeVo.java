package cn.lxinet.lfs.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件树形数据结构
 *
 * @author zcx
 * @date 2023/11/25
 */
@Data
public class FileTreeVo implements Serializable {
    private Long id;
    private String label;
    private boolean isLeaf;
    private List<FileTreeVo> children;

    public FileTreeVo(){

    }

    public FileTreeVo(Long id, String label){
        this.id = id;
        this.label = label;
        children = new ArrayList<>();
    }

}

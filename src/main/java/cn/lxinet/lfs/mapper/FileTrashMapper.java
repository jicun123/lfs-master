package cn.lxinet.lfs.mapper;

import cn.lxinet.lfs.entity.FileTrash;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件回收站
 *
 * @author zcx
 * @date 2023/11/30
 */
@Mapper
public interface FileTrashMapper extends BaseMapper<FileTrash> {

}

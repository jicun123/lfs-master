package cn.lxinet.lfs.mapper;

import cn.lxinet.lfs.entity.FileTrashDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件回收站明细
 *
 * @author zcx
 * @date 2024/03/19
 */
@Mapper
public interface FileTrashDetailMapper extends BaseMapper<FileTrashDetail> {

}

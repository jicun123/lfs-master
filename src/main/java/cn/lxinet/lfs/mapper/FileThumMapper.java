package cn.lxinet.lfs.mapper;

import cn.lxinet.lfs.entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.lxinet.lfs.entity.FileThum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件缩略图
 *
 * @author zcx
 * @date 2023/11/25
 */
@Mapper
public interface FileThumMapper extends BaseMapper<FileThum> {

    @Select("select * from lfs_file_thum where file_md5 = #{fileMd5}")
    List<FileThum> listByFileMd5WithDel(@Param("fileMd5") String fileMd5);

}

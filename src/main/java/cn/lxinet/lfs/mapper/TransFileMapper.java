package cn.lxinet.lfs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.lxinet.lfs.entity.TransFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 转码文件mapper
 *
 * @author zcx
 * @date 2023/12/15
 */
@Mapper
public interface TransFileMapper extends BaseMapper<TransFile> {


    @Update("update lfs_trans_file set md5 = #{md5}, update_time = #{uptime} where id = #{id}")
    void updateMd5(@Param("id") Long id, @Param("md5") String md5, @Param("uptime") Date uptime);

    @Update("update lfs_trans_file set deleted = 1, update_time = #{uptime} where file_id = #{fileId}")
    void deleteByFileId(@Param("fileId") Long fileId, @Param("uptime") Date uptime);

    @Select("select * from lfs_trans_file where file_id = #{fileId}")
    List<TransFile> listByFileIdWithDel(@Param("fileId") Long fileId);
}

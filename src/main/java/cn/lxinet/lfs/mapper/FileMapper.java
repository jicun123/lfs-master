package cn.lxinet.lfs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.lxinet.lfs.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 文件mapper
 *
 * @author zcx
 * @date 2023/11/09
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

    @Update("update lfs_file set trans_status = #{transStatus}, update_time = #{uptime} where id = #{id}")
    void updateTrans(@Param("id") Long id, @Param("transStatus") Integer transStatus, @Param("uptime") Date uptime);

    @Update("update lfs_file set md5 = #{md5}, update_time = #{uptime} where id = #{id}")
    void updateMd5(@Param("id") Long id, @Param("md5") String md5, @Param("uptime") Date uptime);

    @Select("select * from lfs_file where id = #{id}")
    File getFileWithDel(@Param("id") Long id);

}

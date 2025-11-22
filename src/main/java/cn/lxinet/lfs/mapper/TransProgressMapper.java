package cn.lxinet.lfs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.lxinet.lfs.entity.TransProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 转码进度mapper
 *
 * @author zcx
 * @date 2023/11/27
 */
@Mapper
public interface TransProgressMapper extends BaseMapper<TransProgress> {

    @Select("select * from lfs_trans_progress where file_id = #{fileId} and deleted = 0")
    List<TransProgress> queryByFileId(@Param("fileId") Long fileId);

    @Update("update lfs_trans_progress set trans_status = #{transStatus}, update_time = #{uptime} where id = #{id}")
    void updateTrans(@Param("id") Long id, @Param("transStatus") Integer transStatus, @Param("uptime") Date uptime);

    @Update("update lfs_trans_progress set progress = #{progress}, file_trans_id = #{fileTransId}, update_time = #{uptime} where id = #{id}")
    void updateProgress(@Param("id") Long id, @Param("progress") Double progress, Long fileTransId, @Param("uptime") Date uptime);

    @Update("update lfs_trans_progress set deleted = 1, update_time = #{uptime} where file_id = #{fileId}")
    void deleteByFileId(@Param("fileId") Long fileId, @Param("uptime") Date uptime);
}

package cn.lxinet.lfs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.lxinet.lfs.entity.TransTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 转码模板mapper
 *
 * @author zcx
 * @date 2023/11/26
 */
@Mapper
public interface TransTemplateMapper extends BaseMapper<TransTemplate> {

    @Select("select * from lfs_trans_template where status = 1 and deleted = 0")
    List<TransTemplate> queryOpenList();

    @Update("update lfs_trans_template set status = #{status}, update_time = #{uptime} where id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("uptime") Date uptime);

}

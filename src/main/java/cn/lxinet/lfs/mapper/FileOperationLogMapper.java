package cn.lxinet.lfs.mapper;

import cn.lxinet.lfs.entity.FileOperationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件操作记录Mapper
 *
 * @author system
 * @date 2025/11/04
 */
@Mapper
public interface FileOperationLogMapper extends BaseMapper<FileOperationLog> {
}


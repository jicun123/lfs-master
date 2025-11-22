package cn.lxinet.lfs.service.impl;

import cn.lxinet.lfs.entity.FileOperationLog;
import cn.lxinet.lfs.mapper.FileOperationLogMapper;
import cn.lxinet.lfs.service.FileOperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件操作记录服务实现
 *
 * @author system
 * @date 2025/11/04
 */
@Service
public class FileOperationLogServiceImpl implements FileOperationLogService {
    
    @Autowired
    private FileOperationLogMapper logMapper;
    
    @Override
    public void log(FileOperationLog log) {
        logMapper.insert(log);
    }
    
    @Override
    public Object getLogList(Long pageNo, Long pageSize, Long userId, String operation, String keyword) {
        Page<FileOperationLog> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<FileOperationLog> wrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了用户ID，只查该用户的记录
        if (userId != null) {
            wrapper.eq(FileOperationLog::getUserId, userId);
        }
        
        // 如果指定了操作类型
        if (StringUtils.isNotBlank(operation)) {
            wrapper.eq(FileOperationLog::getOperation, operation);
        }
        
        // 搜索文件名
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(FileOperationLog::getFileName, keyword);
        }
        
        wrapper.orderByDesc(FileOperationLog::getCreateTime);
        Page<FileOperationLog> logPage = logMapper.selectPage(page, wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("records", logPage.getRecords());
        result.put("total", logPage.getTotal());
        result.put("current", logPage.getCurrent());
        result.put("size", logPage.getSize());
        
        return result;
    }
}


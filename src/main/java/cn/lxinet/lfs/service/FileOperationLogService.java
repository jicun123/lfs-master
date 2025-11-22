package cn.lxinet.lfs.service;

import cn.lxinet.lfs.entity.FileOperationLog;

/**
 * 文件操作记录服务
 *
 * @author system
 * @date 2025/11/04
 */
public interface FileOperationLogService {
    
    /**
     * 记录文件操作
     * 
     * @param log 操作记录
     */
    void log(FileOperationLog log);
    
    /**
     * 获取操作记录列表（分页）
     * 
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param userId 用户ID（null表示所有用户，仅管理员）
     * @param operation 操作类型（null表示所有类型）
     * @param keyword 搜索关键词（文件名）
     * @return 操作记录列表
     */
    Object getLogList(Long pageNo, Long pageSize, Long userId, String operation, String keyword);
}


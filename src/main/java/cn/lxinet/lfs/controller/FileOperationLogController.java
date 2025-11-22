package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.service.FileOperationLogService;
import cn.lxinet.lfs.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文件操作记录控制器
 *
 * @author system
 * @date 2025/11/04
 */
@RestController
@RequestMapping("/operationLog")
public class FileOperationLogController extends BaseController {
    
    @Autowired
    private FileOperationLogService logService;
    
    /**
     * 获取操作记录列表
     * 所有用户都可以查看所有操作记录（只读）
     * 管理员和普通用户都可以看所有人的记录
     */
    @GetMapping("/list")
    public Result<?> getLogList(@RequestParam(defaultValue = "1") Long pageNo,
                                 @RequestParam(defaultValue = "20") Long pageSize,
                                 @RequestParam(required = false) Long userId,
                                 @RequestParam(required = false) String operation,
                                 @RequestParam(required = false) String keyword) {
        Long currentUserId = getUserId();
        if (currentUserId == null) {
            return error("未登录或登录已过期");
        }
        
        // 所有用户都可以查看所有操作记录
        // 不再限制普通用户只能看自己的记录
        
        return success(logService.getLogList(pageNo, pageSize, userId, operation, keyword));
    }
}


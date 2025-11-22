package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.FileTrash;
import cn.lxinet.lfs.service.FileService;
import cn.lxinet.lfs.service.FileThumService;
import cn.lxinet.lfs.service.FileTrashService;
import cn.lxinet.lfs.utils.MinioUtil;
import cn.lxinet.lfs.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 回收站控制器
 *
 * @author zcx
 * @date 2024/03/16
 */
@RestController
@RequestMapping("/trash")
public class TrashController extends BaseController{
    @Autowired
    private FileTrashService fileTrashService;
    @Autowired
    private FileService fileService;

    @GetMapping("/list")
    public Result list(){
        Page<FileTrashVo> page = fileTrashService.listByPage(getPageNo(), getPageSize());
        return Result.success(page);
    }
    /**
     * 删除文件/文件夹
     *
     * @param ids
     * @return {@link Result}
     */
    @PostMapping("/delete")
    public Result delete(String ids){
        List<String> idList = Arrays.asList(ids.split(","));
        
        // 如果不是管理员，需要检查是否有权限删除这些文件
        if (!isAdmin()) {
            Long currentUserId = getUserId();
            List<FileTrash> trashList = fileTrashService.list(new LambdaQueryWrapper<FileTrash>().in(FileTrash::getId, idList));
            
            for (FileTrash trash : trashList) {
                File file = fileService.getById(trash.getFileId());
                if (file != null) {
                    // 检查是否是文件所有者
                    if (file.getUserId() == null || !file.getUserId().equals(currentUserId)) {
                        return error("无权彻底删除文件：" + file.getName() + "，只能删除自己上传的文件");
                    }
                }
            }
        }
        
        fileTrashService.delete(idList);
        return Result.success();
    }

    /**
     * 回收文件/文件夹
     *
     * @param ids
     * @return {@link Result}
     */
    @PostMapping("/recycle")
    public Result recycle(String ids){
        List<String> idList = Arrays.asList(ids.split(","));
        
        // 如果不是管理员，需要检查是否有权限恢复这些文件
        if (!isAdmin()) {
            Long currentUserId = getUserId();
            List<FileTrash> trashList = fileTrashService.list(new LambdaQueryWrapper<FileTrash>().in(FileTrash::getId, idList));
            
            for (FileTrash trash : trashList) {
                File file = fileService.getById(trash.getFileId());
                if (file != null) {
                    // 检查是否是文件所有者
                    if (file.getUserId() == null || !file.getUserId().equals(currentUserId)) {
                        return error("无权恢复文件：" + file.getName() + "，只能恢复自己上传的文件");
                    }
                }
            }
        }
        
        fileTrashService.recycle(idList);
        return Result.success();
    }


}

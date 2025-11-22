package cn.lxinet.lfs.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.lxinet.lfs.convert.FileConvert;
import cn.lxinet.lfs.dto.UploadChunkDto;
import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.FileOperationLog;
import cn.lxinet.lfs.enums.OperationType;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.service.FileOperationLogService;
import cn.lxinet.lfs.service.FileService;
import cn.lxinet.lfs.service.FileThumService;
import cn.lxinet.lfs.utils.Assert;
import cn.lxinet.lfs.utils.MinioUtil;
import cn.lxinet.lfs.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

/**
 * 文件控制器
 *
 * @author zcx
 * @date 2023/11/09
 */
@RestController
@RequestMapping("/file")
public class FileController extends BaseController{
    @Autowired
    private FileService fileService;
    @Autowired
    private FileThumService fileThumService;
    @Autowired
    private MinioUtil minioUtil;
    @Autowired
    private FileOperationLogService operationLogService;
    @Value("${config.file-server.type}")
    private String fileServerType;

    @PostMapping("/dirAdd")
    public Result dirAdd(Long dirId, String name){
        // 仅管理员可以创建文件夹
        if (!isAdmin()) {
            return error("无权限操作，仅管理员可创建文件夹");
        }
        File dir = fileService.dirAdd(dirId, name);
        return Result.success(FileConvert.INSTANCE.toVo(dir));
    }

    @PostMapping("/rename")
    public Result rename(Long id, String name){
        // 如果不是管理员，需要检查是否有权限重命名
        if (!isAdmin()) {
            Long currentUserId = getUserId();
            File file = fileService.getById(id);
            if (file != null) {
                // 检查是否是文件所有者
                if (file.getUserId() == null || !file.getUserId().equals(currentUserId)) {
                    return error("无权重命名文件：" + file.getName() + "，只能重命名自己上传的文件");
                }
            }
        }
        
        fileService.updateName(id, name);
        return Result.success();
    }

    @GetMapping("/list")
    public Result list(@RequestParam(value = "dirId", required = false, defaultValue = "0") Long dirId,
                       @RequestParam(value = "fileType", required = false, defaultValue = "0") Integer fileType,
                       @RequestParam(value = "key", required = false, defaultValue = "") String key){
        Page<FileVo> page = fileService.listByPage(dirId, fileType, key, getPageNo(), getPageSize());
        return Result.success(page);
    }

    @GetMapping("/dirTree")
    public Result dirTree(@RequestParam(value = "dirId", required = false, defaultValue = "0") Long dirId){
        List<FileTreeVo> list = fileService.dirTree(dirId);
        return Result.success(list);
    }

    @GetMapping("/detail/{fileId}")
    public Result detail(@PathVariable("fileId") Long fileId){
        FileVo fileVo = fileService.getFileVoById(fileId);
        return Result.success(fileVo);
    }

    @PostMapping("/uploadInit")
    public Result uploadInit(@RequestParam(value = "dirId", required = false, defaultValue = "0") Long dirId,
                             String fileName, String md5){
        Assert.isTrue(StringUtils.isNotEmpty(fileName), ErrorCode.PARAM_ERROR, "文件名不能为空");
        Assert.isTrue(StringUtils.isNotEmpty(md5) && md5.length() == 32, ErrorCode.PARAM_ERROR, "md5错误");
        Map<String, Object> data = new HashMap<>();
        data.put("fileServerType", fileServerType);
        if("local".equals(fileServerType)){
            UploadVo uploadVo = fileService.uploadinit(dirId, fileName, md5);
            data.put("upload", uploadVo);
            return new Result<>(data);
        }else if ("minio".equals(fileServerType)){
            String newFileName = UUID.randomUUID() + ".mp4";
            String policyUrl = minioUtil.getPolicyUrl(newFileName);
            data.put("policyUrl", policyUrl);
            data.put("fileName", newFileName);
            return new Result<>(data);
        }
        return Result.success();
    }

    @PostMapping(value="/upload")
    public Result upload(@Valid UploadChunkDto uploadChunk) throws Exception {
        Assert.isTrue(uploadChunk.getChunkTotal() >= uploadChunk.getChunkNumber(), ErrorCode.CHUNK_NUMBER_VERI_FAIL);
        Long fileId = fileService.upload(uploadChunk);
        
        // 如果是最后一片，记录上传者信息和操作日志
        if (fileId > 0) {
            File file = fileService.getById(fileId);
            if (file != null) {
                // 更新文件的上传者信息
                file.setUserId(getUserId());
                file.setUsername(getUsername());
                fileService.updateById(file);
                
                // 记录上传日志
                FileOperationLog log = new FileOperationLog(
                    getUserId(),
                    getUsername(),
                    OperationType.UPLOAD.getCode(),
                    fileId,
                    file.getName(),
                    getFilePathString(file.getDirId()),
                    file.getFileSize(),
                    getIpAddress()
                );
                operationLogService.log(log);
            }
        }
        
        return new Result(ErrorCode.SUCCESS.getCode(), fileId > 0 ? "合并完成" : "分片上传完成", String.valueOf(fileId));
    }

    @GetMapping("/thumList")
    public Result thumList(String md5){
        List<FileThumVo> list = fileThumService.listByMd5(md5);
        return Result.success(list);
    }

    /**
     * 更新文件缩略图
     *
     * @param fileId
     * @param thumId
     * @return {@link Result}
     */
    @PostMapping("/updateFileThum")
    public Result updateFileThum(Long fileId, Long thumId){
        // 如果不是管理员，需要检查是否有权限更新缩略图
        if (!isAdmin()) {
            Long currentUserId = getUserId();
            File file = fileService.getById(fileId);
            if (file != null) {
                // 检查是否是文件所有者
                if (file.getUserId() == null || !file.getUserId().equals(currentUserId)) {
                    return error("无权更新文件缩略图：" + file.getName() + "，只能更新自己上传的文件");
                }
            }
        }
        
        fileService.updateFileThum(fileId, thumId);
        return Result.success();
    }

    /**
     * 移动文件
     *
     * @param fileIds
     * @param dirId
     * @return {@link Result}
     */
    @PostMapping("/move")
    public Result move(String fileIds, Long dirId){
        Assert.isTrue(StringUtils.isNoneBlank(fileIds));
        
        List<String> fileIdList = Arrays.asList(fileIds.split(","));
        
        // 如果不是管理员，需要检查是否有权限移动这些文件
        if (!isAdmin()) {
            Long currentUserId = getUserId();
            for (String fileIdStr : fileIdList) {
                try {
                    Long fileId = Long.parseLong(fileIdStr);
                    File file = fileService.getById(fileId);
                    if (file != null) {
                        // 检查是否是文件所有者
                        if (file.getUserId() == null || !file.getUserId().equals(currentUserId)) {
                            return error("无权移动文件：" + file.getName() + "，只能移动自己上传的文件");
                        }
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效的ID
                }
            }
        }
        
        // 记录移动操作
        for (String fileIdStr : fileIdList) {
            try {
                Long fileId = Long.parseLong(fileIdStr);
                File file = fileService.getById(fileId);
                if (file != null) {
                    String sourcePath = getFilePathString(file.getDirId());
                    String targetPath = getFilePathString(dirId);
                    
                    FileOperationLog log = new FileOperationLog(
                        getUserId(),
                        getUsername(),
                        OperationType.MOVE.getCode(),
                        fileId,
                        file.getName(),
                        sourcePath,
                        file.getFileSize(),
                        getIpAddress()
                    );
                    log.setTargetPath(targetPath);
                    operationLogService.log(log);
                }
            } catch (NumberFormatException e) {
                // 忽略无效的ID
            }
        }
        
        fileService.move(fileIdList, dirId);
        return Result.success();
    }

    /**
     * 手动转码
     *
     * @param fileId
     * @return {@link Result}
     */
    @PostMapping("/manualTranscode")
    public Result manualTranscode(Long fileId){
        // 如果不是管理员，需要检查是否有权限手动转码
        if (!isAdmin()) {
            Long currentUserId = getUserId();
            File file = fileService.getById(fileId);
            if (file != null) {
                // 检查是否是文件所有者
                if (file.getUserId() == null || !file.getUserId().equals(currentUserId)) {
                    return error("无权对文件进行转码：" + file.getName() + "，只能转码自己上传的文件");
                }
            }
        }
        
        fileService.manualTranscode(fileId);
        return Result.success();
    }

    /**
     * 删除文件/文件夹
     *
     * @param fileIds
     * @return {@link Result}
     */
    @PostMapping("/delete")
    public Result delete(String fileIds){
        List<String> fileIdList = Arrays.asList(fileIds.split(","));
        
        // 如果不是管理员，需要检查是否有权限删除这些文件
        if (!isAdmin()) {
            Long currentUserId = getUserId();
            for (String fileIdStr : fileIdList) {
                try {
                    Long fileId = Long.parseLong(fileIdStr);
                    File file = fileService.getById(fileId);
                    if (file != null) {
                        // 检查是否是文件所有者
                        if (file.getUserId() == null || !file.getUserId().equals(currentUserId)) {
                            return error("无权删除文件：" + file.getName() + "，只能删除自己上传的文件");
                        }
                    }
                } catch (NumberFormatException e) {
                    // 忽略无效的ID
                }
            }
        }
        
        fileService.trash(fileIdList);
        return Result.success();
    }

    @PostMapping(value="/minioUpload")
    public Result minioUpload() throws Exception {
        minioUtil.uploadFile();
        return new Result();
    }

    @PostMapping(value="/getDownloadUrl")
    public Result getDownloadUrl(String fileIds) {
        // 记录下载操作
        List<String> fileIdList = Arrays.asList(fileIds.split(","));
        for (String fileIdStr : fileIdList) {
            try {
                Long fileId = Long.parseLong(fileIdStr);
                File file = fileService.getById(fileId);
                if (file != null) {
                    FileOperationLog log = new FileOperationLog(
                        getUserId(),
                        getUsername(),
                        OperationType.DOWNLOAD.getCode(),
                        fileId,
                        file.getName(),
                        getFilePathString(file.getDirId()),
                        file.getFileSize(),
                        getIpAddress()
                    );
                    operationLogService.log(log);
                }
            } catch (NumberFormatException e) {
                // 忽略无效的ID
            }
        }
        
        List<String> urls = fileService.getDownloadUrl(fileIdList, 0);
        return new Result(urls);
    }

    @GetMapping(value = "/filePathList")
    public Result queryPathList(Long dirId) {
        return  Result.success(fileService.getFilePathList(dirId));
    }
    
    /**
     * 获取文件路径字符串（用于日志）
     */
    private String getFilePathString(Long dirId) {
        if (dirId == null || dirId == 0) {
            return "/";
        }
        List<File> pathList = fileService.getFilePathList(dirId);
        if (pathList == null || pathList.isEmpty()) {
            return "/";
        }
        StringBuilder path = new StringBuilder("/");
        for (File dir : pathList) {
            path.append(dir.getName()).append("/");
        }
        return path.toString();
    }
}

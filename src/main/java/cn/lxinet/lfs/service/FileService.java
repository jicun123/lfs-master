package cn.lxinet.lfs.service;

import cn.lxinet.lfs.entity.*;
import cn.lxinet.lfs.enums.FileInTrash;
import cn.lxinet.lfs.event.FileDeleteEvent;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.config.PdfWatermarkConfig;
import cn.lxinet.lfs.convert.FileConvert;
import cn.lxinet.lfs.convert.TransProgressConvert;
import cn.lxinet.lfs.dto.UploadChunkDto;
import cn.lxinet.lfs.enums.EventTransType;
import cn.lxinet.lfs.enums.FileTransStatus;
import cn.lxinet.lfs.enums.FileType;
import cn.lxinet.lfs.event.TransEvent;
import cn.lxinet.lfs.mapper.FileMapper;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.Assert;
import cn.lxinet.lfs.utils.FileUtil;
import cn.lxinet.lfs.utils.PdfUtil;
import cn.lxinet.lfs.utils.VideoUtil;
import cn.lxinet.lfs.vo.FileTreeVo;
import cn.lxinet.lfs.vo.FileVo;
import cn.lxinet.lfs.vo.TransProgressVo;
import cn.lxinet.lfs.vo.UploadVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ws.schild.jave.info.MultimediaInfo;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件服务
 *
 * @author zcx
 * @date 2023/11/09
 */
@Service
public class FileService extends ServiceImpl<FileMapper, File> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileService.class);
    @Value("${config.trash-recycle-days}")
    private Integer trashRetainDays;
    @Autowired
    private FileMapper fileMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private TransTemplateService transTemplateService;
    @Autowired
    private TransFileService transFileService;
    @Autowired
    private FileTrashDetailService fileTrashDetailService;
    @Autowired
    private FileThumService fileThumService;
    @Autowired
    private TransProgressService transProgressService;
    @Autowired
    private FileTrashService fileTrashService;
    @Autowired
    private PdfWatermarkConfig pdfWatermarkConfig;
    @Autowired
    private ApplicationContext applicationContext;

    public UploadVo uploadinit(Long dirId, String fileName, String md5){
        File file = findByMd5(md5);
        if (file == null){
            return new UploadVo(createUploadId());
        }else {
            //md5文件已存在
            java.io.File localFile = new java.io.File(fileConfig.getLocalFileDir() + file.getPath());
            if (localFile.exists()){
                //本地文件存在，秒传
                String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
                Long fileId = fastUpload(dirId, file, fileName, suffix);
                return new UploadVo(true, fileId);
            }else {
                return new UploadVo(createUploadId());
            }
        }
    }

    private String createUploadId(){
        String uploadId = UUID.randomUUID().toString();
        String redisKey = fileConfig.getUploadIdKey(uploadId);
        boolean success = redisService.set(redisKey, uploadId, 24 * 60 * 60);
        if (!success) {
            LOGGER.error("上传文件初始化失败，Redis写入失败，uploadId：{}，key：{}", uploadId, redisKey);
            throw new RuntimeException("文件上传初始化失败，请检查Redis服务是否正常");
        }
        // 验证是否真的写入成功
        boolean exists = redisService.exists(redisKey);
        if (!exists) {
            LOGGER.error("上传文件初始化失败，Redis验证失败，uploadId：{}，key：{}", uploadId, redisKey);
            throw new RuntimeException("文件上传初始化失败，Redis验证失败");
        }
        LOGGER.info("上传文件初始化完成，uploadId：{}，key：{}", uploadId, redisKey);
        return uploadId;
    }

    public Long upload(UploadChunkDto chunk) throws IOException {
        String redisKey = fileConfig.getUploadIdKey(chunk.getUploadId());
        boolean exists = redisService.exists(redisKey);
        if (!exists) {
            LOGGER.error("uploadId验证失败，Redis中不存在该key，uploadId：{}，key：{}，文件名：{}", 
                chunk.getUploadId(), redisKey, chunk.getFileName());
        }
        Assert.isTrue(exists, ErrorCode.UPLOADID_VOID);
        if (chunk.getChunkTotal() > 1){
            //分片上传（至少2个分片）
            return uploadChunk(chunk);
        }
        LOGGER.info("上传文件：{}，uploadId：{}，只有一个分片，直接上传", chunk.getFileName(), chunk.getUploadId());
        //就1个分片（即没分片），直接上传完成
        String fileName = chunk.getFileName();
        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
        String localPath = fileConfig.getUploadFilePath(suffix);
        java.io.File file = new java.io.File(localPath);
        chunk.getFile().transferTo(file);
        redisService.del(fileConfig.getUploadIdKey(chunk.getUploadId()));
        Long fileId = uploadSuccess(chunk.getDirId(), chunk.getUploadId(), chunk.getMd5(), localPath, fileName, file.length(), suffix);
        LOGGER.info("上传文件：{}，uploadId：{}，只有一个分片，直接上传，完成", chunk.getFileName(), chunk.getUploadId());
        return fileId;
    }

    public Long uploadChunk(UploadChunkDto chunk) throws IOException {
        LOGGER.info("上传文件：{}，uploadId：{}，分片上传：{}/{}", chunk.getFileName(), chunk.getUploadId(), chunk.getChunkNumber(), chunk.getChunkTotal());
        //分片上传（至少2个分片）
        String uploadId = chunk.getUploadId();
        chunk.getFile().transferTo(fileConfig.getTempChunkFile(uploadId, chunk.getChunkNumber()));
        LOGGER.info("上传文件：{}，uploadId：{}，分片上传：{}/{}，完成", chunk.getFileName(), chunk.getUploadId(), chunk.getChunkNumber(), chunk.getChunkTotal());
        Long fileId = 0L;
        if (fileConfig.getLocalTempChunkNum(uploadId).intValue() == chunk.getChunkTotal().intValue()){
            //上传完成，合并操作
            fileId = mergeFile(chunk.getDirId(), uploadId, chunk.getMd5(), chunk.getFileName());
            redisService.del(fileConfig.getUploadIdKey(chunk.getUploadId()));
        }
        return fileId;
    }

    private Long mergeFile(Long dirId, String uploadId, String md5, String fileName) {
        LOGGER.info("上传文件：{}，uploadId：{}，分片上传，合并文件，开始", fileName, uploadId);
        String suffix = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
        byte[] buff = new byte[1024];
        int len = 0;
        FileInputStream in = null;
        BufferedOutputStream outputStream = null;
        Long fileId = 0L;
        try {
            java.io.File tempDirFile = new java.io.File(fileConfig.getUploadTempDir(uploadId));
            int chunkTotal = tempDirFile.list().length;
            String localPath = fileConfig.getUploadFilePath(suffix);
            java.io.File targetFile = new java.io.File(localPath);
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
            for (int i = 0; i < chunkTotal; i ++) {
                java.io.File tempFile = fileConfig.getTempChunkFile(uploadId, i + 1);
                in = new FileInputStream(tempFile);
                while ((len = in.read(buff, 0, 1024)) > 0) {
                    outputStream.write(buff, 0, len);
                    outputStream.flush();
                }
                in.close();
                tempFile.delete();
            }
            tempDirFile.delete();
            fileId = uploadSuccess(dirId, uploadId, md5, localPath, fileName, targetFile.length(), suffix);
            LOGGER.info("上传文件：{}，uploadId：{}，分片上传，合并文件，完成", fileName, uploadId);
        } catch (Exception e) {
            LOGGER.error("上传文件：" + fileName + "，uploadId：" + uploadId + "，分片上传，合并文件出现异常", e);
        } finally {
            try {
                if (null != in){
                    in.close();
                }
                if (null != outputStream){
                    outputStream.close();
                }
            }catch (Exception e){
                LOGGER.error("上传文件：" + fileName + "，uploadId：" + uploadId + "，分片上传，合并文件关闭文件流出现异常", e);
            }
        }
        return fileId;
    }

    /**
     * 秒传
     * @param oldFile
     * @param fileName
     * @param suffix
     * @return
     */
    private Long fastUpload(Long dirId, File oldFile, String fileName, String suffix){
        if (dirId != 0){
            File dir = getById(dirId);
            Assert.isTrue(dir != null && dir.getIsDir() == 1, ErrorCode.FILE_DIR_NOT_EXIST);
        }
        LOGGER.info("文件秒传，md5：{}，oldFileId：{}", oldFile.getMd5(), oldFile.getId());
        String localPath = fileConfig.getLocalFileDir() + oldFile.getPath();
        //获取所有模板最后一次更新的更新时间
        Date lastUpdateTime = transTemplateService.getLastUpdatedTime();
        //如果原文件转码成功，且所有模板在原文件上传之前更新的，则说明模板没有改变，不需要重新转码，使用原文件的转码文件即可
        if (oldFile.getTransStatus().equals(FileTransStatus.TRANS_SUCCESS.getStatus()) && lastUpdateTime != null && lastUpdateTime.before(oldFile.getCreateTime())){
            LOGGER.info("文件秒传，所有模板在原文件上传之前更新，无需再重新转码，md5：{}，oldFileId：{}", oldFile.getMd5(), oldFile.getId());
            Long fileId = saveFile(dirId, oldFile.getMd5(), fileName, oldFile.getFileSize(), suffix, oldFile.getPath(), oldFile.getDuration(), FileTransStatus.TRANS_SUCCESS);
            //生成转码文件记录
            QueryWrapper<TransFile> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("file_size", "suffix", "md5", "path").eq("file_id", oldFile.getId());
            List<TransFile> transList = transFileService.list(queryWrapper);
            transList.forEach(trans -> {
                TransFile transFile = new TransFile(fileId, trans.getMd5(), trans.getFileSize(), trans.getSuffix(), trans.getPath());
                transFileService.save(transFile);
                Long time = System.currentTimeMillis();
                transProgressService.save(new TransProgress(fileId, trans.getSuffix().replace(".", ""), 100d, transFile.getId(), FileTransStatus.TRANS_SUCCESS.getStatus(), time, time));
            });
            //如果是视频，生成截图
            if (FileUtil.isVideo(suffix)){
                applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.GEN_VIDEO, FileType.VIDEO, localPath, oldFile.getMd5(), oldFile.getDuration()));
            }else if (FileUtil.isDocument(suffix)){
                String pdfLocalPath = FileUtil.isPdf(suffix) ? fileConfig.getLocalFileDir() + oldFile.getPath() : "";
                if (!transList.isEmpty()){
                    pdfLocalPath = fileConfig.getLocalFileDir() + transList.get(0).getPath();
                }
                applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.GEN_PDF, FileType.DOCUMENT, pdfLocalPath, oldFile.getMd5()));
            }
            return fileId;
        }else {
            //在原文件上传之后，模板有更新过，重新转码
            return uploadSuccess(dirId, "", oldFile.getMd5(), localPath, fileName, oldFile.getFileSize(), suffix, oldFile.getDuration());
        }
    }

    /**
     * 上传成功后，入库、md5计算、转码等功能
     * @param uploadId
     * @param localPath
     * @param fileName
     * @param fileSize
     * @param suffix
     * @param duration
     * @return
     */
    private Long uploadSuccess(Long dirId, String uploadId, String md5, String localPath, String fileName, Long fileSize, String suffix, Long duration){
        //在windows系统下，保存数据库文件路径，要把\改成/
        String filePath = localPath.replace(fileConfig.getLocalFileDir(), "").replace("\\", "/");
        Long fileId = saveFile(dirId, md5, fileName, fileSize, suffix, filePath, duration);
        toTranscode(fileId, md5, duration, localPath, suffix);
        if (StringUtils.isNoneBlank(uploadId)){
            redisService.del(fileConfig.getUploadIdKey(uploadId));
        }
        return fileId;
    }

    private Long uploadSuccess(Long dirId, String uploadId, String md5, String localPath, String fileName, Long fileSize, String suffix){
        Long duration = getDuration(localPath, suffix);
        return uploadSuccess(dirId, uploadId, md5, localPath, fileName, fileSize, suffix, duration);
    }

    private Long getDuration(String localPath, String suffix){
        if (!FileUtil.isVideo(suffix)){
            return 0L;
        }
        MultimediaInfo medisInfo = VideoUtil.getVideoInfo(localPath);
        Long duration = medisInfo == null ? 0 : medisInfo.getDuration() / 1000;
        return duration;
    }

    /**
     * 转码
     *
     * @param fileId
     * @param md5
     * @param duration
     * @param localPath
     * @param suffix
     */
    private void toTranscode(Long fileId, String md5, Long duration, String localPath, String suffix){
        if (FileUtil.isVideo(suffix)){
            LOGGER.info("视频转码消息发布，fileId：{}，文件路径：{}", fileId, localPath);
            applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.GEN_VIDEO, FileType.VIDEO, localPath, md5, duration));
            applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.TO_MP4, FileType.VIDEO, localPath));
        }else if (FileUtil.isDocument(suffix)){
            LOGGER.info("文档转码消息发布，fileId：{}，文件路径：{}", fileId, localPath);
            if (FileUtil.isPdf(suffix)){
                //如果数pdf文件，直接生成封面，不需要转码
                updateTrans(fileId, FileTransStatus.NO_NEED_TRANS.getStatus());
                applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.GEN_PDF, FileType.DOCUMENT, localPath, md5));
            }else {
                applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.TO_PDF, FileType.DOCUMENT, localPath, md5));
            }
            LOGGER.info("文档转码消息发布完成，fileId：{}", fileId);
        }else {
            //其他文件无法转码和转hls
            updateTrans(fileId, FileTransStatus.NO_SUPPORT_TRANS.getStatus());
        }
    }

    /**
     * 手动转码
     *
     * @param fileId 文件 ID
     */
    @Transactional
    public void manualTranscode(Long fileId){
        LOGGER.info("手动转码开始，fileId：{}", fileId);
        File file = getById(fileId);
        Assert.notNull(file, ErrorCode.FILE_NOT_EXIST);
        Assert.isTrue(file.getInTrash().equals(FileInTrash.NO.getValue()), ErrorCode.FILE_NOT_EXIST);
        Assert.isTrue(!file.getTransStatus().equals(FileTransStatus.NO_NEED_TRANS.getStatus()), ErrorCode.FILE_NOT_NEED_TRANS);
        Assert.isTrue(!file.getTransStatus().equals(FileTransStatus.NO_SUPPORT_TRANS.getStatus()), ErrorCode.FILE_NOT_SUPPORT_TRANS);
        //文件上传后2小时内，处于转码中，无法重新转码
        if (System.currentTimeMillis() - file.getCreateTime().getTime() < 2 * 60 * 60 * 1000){
            Assert.isTrue(!file.getTransStatus().equals(FileTransStatus.TRANS.getStatus()), ErrorCode.TRANS_IN_PROGRESS_CANNOT_TRANS_AGAIN);
        }
        //先把原先的转码文件记录、转码进度删除
        transFileService.deleteByFileId(fileId);
        transProgressService.deleteByFileId(fileId);
        //修改为转码中
        updateTrans(fileId, FileTransStatus.TRANS.getStatus());
        toTranscode(fileId, file.getMd5(), file.getDuration(), fileConfig.getLocalFileDir() + file.getPath(), file.getSuffix());
    }

    /**
     * 分页查询文件列表
     *
     * @param dirId 文件夹id
     * @param fileType 文件类型
     * @param key 搜索关键词
     * @param current 当前
     * @param size    大小
     * @return {@link Page}<{@link FileVo}>
     */
    public Page<FileVo> listByPage(Long dirId, Integer fileType, String key, Long current, Long size){
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getInTrash, FileInTrash.NO.getValue());
        if (StringUtils.isBlank(key) && fileType == 0){
            wrapper.eq(File::getDirId, dirId);
        }else {
            //如果有搜索关键词或者分类搜索，就不限制目录
            if (StringUtils.isNotBlank(key)){
                wrapper.like(File::getName, key);
            }
            if (fileType > 0){
                wrapper.eq(File::getFileType, fileType);
            }
        }
        wrapper.orderByDesc(File::getIsDir, File::getId);
        Page<File> page = page(new Page(current, size), wrapper);
        Page<FileVo> voPage = new Page<>(current, size, page.getTotal());
        List<FileVo> voList = FileConvert.INSTANCE.toVoList(page.getRecords());
        if (StringUtils.isNotBlank(key) || fileType > 0){
            Map<Long, String> dirNameMap = new HashMap<>();
            List<Long> dirIds = page.getRecords().stream().map(File :: getDirId).filter(did -> did > 0).collect(Collectors.toList());
            if (!dirIds.isEmpty()){
                List<File> dirList = list(new QueryWrapper<File>().select("id", "name").in("id", dirIds));
                dirNameMap = dirList.stream().collect(Collectors.toMap(File :: getId, File :: getName));
            }
            Map<Long, String> finalDirNameMap = dirNameMap;
            voList.forEach(vo -> vo.setDirName(finalDirNameMap.getOrDefault(vo.getDirId(), "所有文件")));
        }
        voList.forEach(vo -> {
            setTumUrl(vo);
            setVoFileType(vo);
        });
        voPage.setRecords(voList);
        return voPage;
    }


    /**
     * 文件夹树
     *
     * @param dirId 查询该文件夹下对文件夹
     * @return {@link List}<{@link FileTreeVo}>
     */
    public List<FileTreeVo> dirTree(Long dirId){
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(File::getId, File::getName);
        wrapper.eq(File::getInTrash, FileInTrash.NO.getValue()).eq(File::getDirId, dirId).eq(File::getIsDir, 1);
        wrapper.orderByDesc(File::getIsDir, File::getId);
        List<File> files = list(wrapper);
        List<FileTreeVo> fileTree = new ArrayList<>();
        if (files.isEmpty()){
            return fileTree;
        }
        //查下子目录
        List<Long> fileIds = files.stream().map(File :: getId).collect(Collectors.toList());
        LambdaQueryWrapper<File> subWrapper = new LambdaQueryWrapper<>();
        wrapper.select(File::getId, File::getName, File::getDirId);
        wrapper.eq(File::getInTrash, FileInTrash.NO.getValue()).in(File::getDirId, fileIds).eq(File::getIsDir, 1);
        wrapper.orderByDesc(File::getIsDir, File::getId);
        List<File> subFiles = list(subWrapper);
        files.forEach(file -> fileTree.add(new FileTreeVo(file.getId(), file.getName())));
        fileTree.forEach(tree -> subFiles.forEach(subTree -> {
            if (subTree.getDirId().equals(tree.getId())){
                tree.getChildren().add(new FileTreeVo(subTree.getId(), subTree.getName()));
            }
        }));
        fileTree.forEach(tree -> {
            tree.setLeaf(tree.getChildren().isEmpty());
            tree.getChildren().forEach(subTree -> subTree.setLeaf(subTree.getChildren().isEmpty()));
        });
        return fileTree;
    }

    public FileVo getFileVoById(Long id){
        File file = getById(id);
        Assert.notNull(file, ErrorCode.FILE_NOT_EXIST);
        Assert.isTrue(file.getInTrash().equals(FileInTrash.NO.getValue()), ErrorCode.FILE_NOT_EXIST);
        FileVo fileVo = FileConvert.INSTANCE.toVo(file);
        setTumUrl(fileVo);
        setVoFileType(fileVo);
        if (FileUtil.isDocument(file.getSuffix()) || FileUtil.isImage(file.getSuffix())){
            fileVo.setPdfWatermark(PdfUtil.getWmContent(pdfWatermarkConfig));
        }
        if (FileUtil.isVideo(file.getSuffix()) || FileUtil.isAudio(file.getSuffix()) || FileUtil.isPdf(file.getSuffix()) || FileUtil.isImage(file.getSuffix())){
            //视频、音频、pdf、图片 使用原文件预览
            fileVo.setPreviewUrl(fileConfig.getPreviewUrl(fileVo.getPath()));
        }else {
            if (file.getTransStatus().equals(FileTransStatus.TRANS_SUCCESS.getStatus())){
                List<TransFile> fileTransList = transFileService.list(new QueryWrapper<TransFile>().select("file_id", "path")
                        .in("file_id", Collections.singletonList(id)));
                if (!fileTransList.isEmpty()){
                    fileVo.setPreviewUrl(fileConfig.getPreviewUrl(fileTransList.get(0).getPath()));
                }
            }
        }
        List<TransProgress> progressList = transProgressService.list(new QueryWrapper<TransProgress>().select("id", "file_trans_id", "format", "progress", "trans_status", "start_time", "end_time").eq("file_id", id));
        if (!progressList.isEmpty()){
            List<TransProgressVo> progressVoList = TransProgressConvert.INSTANCE.toVoList(progressList);
            List<Long> fileTransIds = progressVoList.stream().map(TransProgressVo::getFileTransId).collect(Collectors.toList());
            List<TransFile> fileTransList = transFileService.list(new QueryWrapper<>(TransFile.class).in("id", fileTransIds));
            Map<Long, TransFile> transMap = fileTransList.stream().collect(Collectors.toMap(TransFile :: getId, vo -> vo));
            progressVoList.forEach(vo -> {
                if (!transMap.containsKey(vo.getFileTransId())){
                    return;
                }
                TransFile trans = transMap.get(vo.getFileTransId());
                vo.setFileSize(trans.getFileSize());
                vo.setPreviewUrl(fileConfig.getPreviewUrl(trans.getPath()));
            });
            fileVo.setProgressList(progressVoList);
        }else {
            fileVo.setProgressList(new ArrayList<>());
        }
        return fileVo;
    }

    public List<File> getFilePathList(Long dirId){
        List<File> dirList = new ArrayList<>();
        File dir = getOne(new LambdaQueryWrapper<File>().select(File::getId, File::getName,File::getDirId).eq(File::getId, dirId).eq(File::getInTrash, FileInTrash.NO.getValue()));
        if (dir == null){
            return dirList;
        }
        dirList.add(dir);
        getFilePathList(dirList, dir);
        Collections.reverse(dirList);
        return dirList;
    }

    public void getFilePathList(List<File> dirList, File file){
        if (file.getDirId() == 0){
            return;
        }
        File dir = getOne(new LambdaQueryWrapper<File>().select(File::getId, File::getName,File::getDirId).eq(File::getId, file.getDirId()).eq(File::getInTrash, FileInTrash.NO.getValue()));
        if (dir == null){
            return;
        }
        dirList.add(dir);
        getFilePathList(dirList, dir);
    }


    public List<String> getDownloadUrl(List<String> ids, Integer type){
        List<File> files = list(new LambdaQueryWrapper<File>().in(File::getId, ids).eq(File::getInTrash, FileInTrash.NO.getValue()));
        List<String> urls = new ArrayList<>();
        if (type == 0){
            //原文件下载
            files.forEach(file -> urls.add(fileConfig.getDownloadUrl(file.getName(), file.getPath())));
        }
        return urls;
    }

    private Long saveFile(Long dirId, String md5, String fileName, Long fileSize, String suffix, String path, Long duration){
        return saveFile(dirId, md5, fileName, fileSize, suffix, path, duration, FileTransStatus.TRANS);
    }

    private Long saveFile(Long dirId, String md5, String fileName, Long fileSize, String suffix, String path, Long duration, FileTransStatus transStatus){
        File file = new File(dirId, md5, fileName, fileSize, suffix, path, duration, transStatus.getStatus(), getFileType(suffix));
        save(file);
        return file.getId();
    }

    public File dirAdd(Long dirId, String name){
        if (dirId != 0){
            File dir = getById(dirId);
            Assert.isTrue(dir != null && dir.getIsDir() == 1, ErrorCode.FILE_DIR_NOT_EXIST);
        }
        File dir = new File();
        dir.setDirId(dirId);
        dir.setIsDir(1);
        dir.setName(name);
        dir.setTransStatus(FileTransStatus.NO_NEED_TRANS.getStatus());
        save(dir);
        return dir;
    }

    /**
     * 扔进回收站
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)
    public void trash(List<String> ids){
        ids.forEach(idStr -> {
            Long id = Long.parseLong(idStr);
            File file = getById(id);
            if (FileInTrash.YES.getValue().equals(file.getInTrash()) || file.getDeleted() == 1){
                return;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, trashRetainDays);
            //进入回收站
            FileTrash fileTrash = new FileTrash(file.getId(), trashRetainDays, calendar.getTimeInMillis(), 0L);
            List<FileTrashDetail> trashDetailList = new ArrayList<>();
            fileTrashService.save(fileTrash);
            if (file.getIsDir() == 1){
                trashDir(id, fileTrash.getId(), trashDetailList, calendar.getTimeInMillis());
            }else {
                trashFile(id);
                trashDetailList.add(new FileTrashDetail(fileTrash.getId(), id));
            }
            fileTrashDetailService.saveBatch(trashDetailList);
        });
    }

    public void trashFile(Long id){
        update(new LambdaUpdateWrapper<File>().set(File::getInTrash, FileInTrash.YES.getValue()).eq(File::getId, id));
    }

    public void trashDir(Long id, Long trashId, List<FileTrashDetail> trashDetailList, Long expireTime){
        List<File> files = list(new LambdaQueryWrapper<File>().eq(File::getDirId, id).eq(File::getInTrash, FileInTrash.NO.getValue()).eq(File::getDeleted, 0));
        //先遍历删除，如果数据量大的话，需要优化，改成批量删除
        files.forEach(file -> {
            if (file.getIsDir() == 1){
                trashDir(file.getId(), trashId, trashDetailList, expireTime);
            }else {
                trashFile(file.getId());
                trashDetailList.add(new FileTrashDetail(trashId, file.getId()));
            }
        });
        trashFile(id);
        trashDetailList.add(new FileTrashDetail(trashId, id));
    }

    /**
     * 彻底删除文件
     * @param fileIds
     */
    public void delete(List<Long> fileIds){
        if (fileIds.isEmpty()){
            return;
        }
        LambdaUpdateWrapper<File> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(File::getDeleted, 1).in(File::getId, fileIds);
        update(updateWrapper);
        //转码文件记录、转码进度删除
        transFileService.deleteByFileIds(fileIds);
        transProgressService.deleteByFileIds(fileIds);
        applicationContext.publishEvent(new FileDeleteEvent(this, fileIds));
    }

    public void updateTrans(Long id, Integer transStatus){
        fileMapper.updateTrans(id, transStatus, new Date());
    }

    public void updateMd5(Long id, String md5){
        fileMapper.updateMd5(id, md5, new Date());
    }

    public File findByMd5(String md5){
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getMd5, md5).eq(File::getInTrash, FileInTrash.NO.getValue()).orderByDesc(File::getId);
        //兼容不同数据库，使用mybatisplus分页查询最后一条记录
        Page<File> page = page(new Page<>(1, 1), wrapper);
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    public File findByMd5WithTrash(String md5){
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getMd5, md5).orderByDesc(File::getId);
        //兼容不同数据库，使用mybatisplus分页查询最后一条记录
        Page<File> page = page(new Page<>(1, 1), wrapper);
        return page.getRecords().isEmpty() ? null : page.getRecords().get(0);
    }

    public void updateFileThum(Long fileId, Long thumId){
        FileThum fileThum = fileThumService.getById(thumId);
        Assert.notNull(fileThum, ErrorCode.FILE_THUM_NOT_EXIST);
        updateFileThum(fileId, fileThum.getPath());
    }

    public void updateFileThum(Long fileId, String thumPath){
        File file = getById(fileId);
        Assert.notNull(file, ErrorCode.FILE_NOT_EXIST);
        Assert.isTrue(file.getInTrash().equals(FileInTrash.NO.getValue()), ErrorCode.FILE_NOT_EXIST);
        update(new LambdaUpdateWrapper<File>().set(File::getThumPath, thumPath).set(File::getUpdateTime, new Date()).eq(File::getId, fileId).eq(File::getInTrash, FileInTrash.NO.getValue()));
    }

    public void updateDuration(Long fileId, Long duration){
        update(new LambdaUpdateWrapper<File>().set(File::getDuration, duration).set(File::getUpdateTime, new Date()).eq(File::getId, fileId));
    }

    public void updateName(Long fileId, String name){
        Assert.isTrue(StringUtils.isNotEmpty(name), ErrorCode.FILE_NAME_CANNOT_EMPTY);
        Assert.isTrue(name.length() <= 200, ErrorCode.FILE_NAME_CANNOT_EXCEED_200_CHARACTERS);
        update(new LambdaUpdateWrapper<File>().set(File::getName, name).eq(File::getId, fileId).eq(File::getInTrash, FileInTrash.NO.getValue()));
    }

    public void move(List<String> fileIds, Long targetDirId){
        //如果移动文件夹，需要判断移动的文件夹不能在原文件夹以及原文件子文件夹下
        if (targetDirId > 0){
            Map<Long, Long> allParentIdMap = new HashMap<>();
            getAllParentIdMap(allParentIdMap, targetDirId);
            Assert.isTrue(!allParentIdMap.isEmpty(), ErrorCode.FILE_DIR_NOT_EXIST);
            fileIds.forEach(fileId -> Assert.isTrue(!allParentIdMap.containsKey(Long.parseLong(fileId)), ErrorCode.FILE_DIR_MOVE_NOT_SELF_OR_SUBDIR));
        }
        update(new LambdaUpdateWrapper<File>().set(File::getDirId, targetDirId).in(File::getId, fileIds).eq(File::getInTrash, FileInTrash.NO.getValue()));
    }

    private void getAllParentIdMap(Map<Long, Long> parentIdMap, Long id){
        if (id == 0){
            return;
        }
        File file = getOne(new LambdaQueryWrapper<File>().select(File::getId, File::getDirId).eq(File::getId, id).eq(File::getInTrash, FileInTrash.NO.getValue()));
        if(file == null){
            return;
        }
        parentIdMap.put(file.getId(), file.getId());
        if (file.getDirId() > 0L){
            getAllParentIdMap(parentIdMap, file.getDirId());
        }
    }

    private void setTumUrl(FileVo vo){
        if (FileUtil.isImage(vo.getSuffix())){
            vo.setThumUrl(fileConfig.getPreviewUrl(vo.getPath()));
        }else {
            vo.setThumUrl(fileConfig.getPreviewUrl(vo.getThumPath()));
        }
    }

    private void setVoFileType(FileVo vo){
        vo.setFileType(getFileType(vo.getSuffix()));
    }

    public Integer getFileType(String suffix){
        if (FileUtil.isVideo(suffix)){
            return FileType.VIDEO.getValue();
        }else if (FileUtil.isAudio(suffix)){
            return FileType.AUDIO.getValue();
        }else if (FileUtil.isDocument(suffix)){
            return FileType.DOCUMENT.getValue();
        }else if (FileUtil.isImage(suffix)){
            return FileType.IMAGE.getValue();
        }else {
            return FileType.OTHER.getValue();
        }
    }

    public File getFileWithDel(Long id){
        return fileMapper.getFileWithDel(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void recycle(List<Long> idList, Long fileId){
        File file = getById(fileId);
        Assert.notNull(file, "文件异常，无法还原");
        long dirId = file.getDirId();
        long newDirId = dirId;
        if (dirId > 0){
            //查询被还原文件的上级文件夹
            File dirFile = getFileWithDel(dirId);
            //如果上级文件夹被删除了，则重新创建
            if (dirFile != null && (dirFile.getDeleted() == 1 || dirFile.getInTrash().equals(FileInTrash.YES.getValue()))){
                //创建上级文件夹，得到新的上级文件夹id
                newDirId = recycleDir(dirId);
                if (dirFile.getDirId() > 0){
                    recycleDirEic(dirFile.getDirId(), newDirId);
                }
            }
        }
        update(new LambdaUpdateWrapper<File>().set(File::getInTrash, FileInTrash.NO.getValue()).in(File::getId, idList));
        update(new LambdaUpdateWrapper<File>().set(File::getDirId, newDirId).in(File::getId, fileId));
    }

    private void recycleDirEic(Long dirId, Long newDirId){
        File parentFile = getFileWithDel(dirId);
        if (parentFile != null && (parentFile.getDeleted() == 1 || parentFile.getInTrash().equals(FileInTrash.YES.getValue()))){
            Long parentDirId = recycleDir(dirId);
            update(new LambdaUpdateWrapper<File>().set(File::getDirId, parentDirId).eq(File::getId, newDirId));
            recycleDirEic(parentFile.getId(), parentDirId);
        }
    }

    /**
     * 创建上级文件夹
     * @param dirId
     * @return
     */
    private Long recycleDir(Long dirId){
        //上级文件夹被删除了，要重新创建
        File dirFile = getFileWithDel(dirId);
        Assert.notNull(dirFile, "上级文件夹异常，无法还原");
        dirFile.setId(null);
        dirFile.setDeleted(0);
        dirFile.setInTrash(0);
        dirFile.setCreateTime(new Date());
        dirFile.setUpdateTime(new Date());
        save(dirFile);
        return dirFile.getId();
    }
}

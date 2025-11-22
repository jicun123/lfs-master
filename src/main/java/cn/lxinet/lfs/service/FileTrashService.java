package cn.lxinet.lfs.service;

import cn.lxinet.lfs.convert.FileTrashConvert;
import cn.lxinet.lfs.convert.TransProgressConvert;
import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.FileTrash;
import cn.lxinet.lfs.entity.FileTrashDetail;
import cn.lxinet.lfs.entity.TransProgress;
import cn.lxinet.lfs.mapper.FileTrashMapper;
import cn.lxinet.lfs.vo.FileTrashVo;
import cn.lxinet.lfs.vo.FileVo;
import cn.lxinet.lfs.vo.TransProgressVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件回收站
 *
 * @author zcx
 * @date 2024/03/16
 */
@Slf4j
@Service
public class FileTrashService extends ServiceImpl<FileTrashMapper, FileTrash> {
    @Autowired
    private FileTrashMapper fileTrashMapper;
    @Autowired
    @Lazy
    private FileService fileService;
    @Autowired
    private FileTrashDetailService fileTrashDetailService;
    @Value("${config.trash-recycle-days}")
    private Integer trashRecycleDays;

    /**
     * 分页查询列表
     *
     * @param current 当前页码
     * @param size    每页数量
     * @return {@link Page}<{@link FileVo}>
     */
    public Page<FileTrashVo> listByPage(long current, long size){
        Page<FileTrash> page = page(new Page(current, size), new LambdaQueryWrapper<FileTrash>().orderByDesc(FileTrash::getId));
        Page<FileTrashVo> voPage = new Page<>(current, size, page.getTotal());
        if (page.getRecords().isEmpty()){
            return voPage;
        }
        List<FileTrashVo> voList = FileTrashConvert.INSTANCE.toVoList(page.getRecords());
        List<Long> fildIds = voList.stream().map(FileTrashVo::getFileId).collect(Collectors.toList());
        List<File> fileList = fileService.list(new LambdaQueryWrapper<File>()
                .select(File::getId, File::getName, File::getIsDir, File::getFileSize, File::getFileType)
                .in(File::getId, fildIds));
        Map<Long, File> fileMap = new HashMap<>();
        fileList.forEach(file -> fileMap.put(file.getId(), file));
        voList.forEach(vo -> {
            File file = fileMap.get(vo.getFileId());
            if (file == null){
                return;
            }
            vo.setFileName(file.getName());
            vo.setFileSize(file.getFileSize());
            vo.setIsDir(file.getIsDir());
            vo.setFileType(file.getFileType());
        });
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 彻底删除
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<String> ids){
        List<FileTrash> list = list(new LambdaQueryWrapper<FileTrash>().in(FileTrash::getId, ids));
        list.forEach(trash -> {
            LambdaQueryWrapper<FileTrashDetail> wrapper = new LambdaQueryWrapper<FileTrashDetail>().in(FileTrashDetail::getTrashId, trash.getId());
            List<FileTrashDetail> detailList = fileTrashDetailService.list(wrapper);
            List<Long> fileIds = detailList.stream().map(FileTrashDetail::getFileId).toList();
            fileService.delete(fileIds);
            fileTrashDetailService.delete(detailList.stream().map(FileTrashDetail::getId).toList());
        });
        LambdaUpdateWrapper<FileTrash> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(FileTrash::getDeleted, 1).in(FileTrash::getId, list.stream().map(FileTrash::getId).toList());
        update(updateWrapper);
    }

    /**
     * 文件回收（还原）
     * @param ids
     */
    @Transactional(rollbackFor = Exception.class)
    public void recycle(List<String> ids){
        List<FileTrash> list = list(new LambdaQueryWrapper<FileTrash>().in(FileTrash::getId, ids));
        list.forEach(trash -> {
            LambdaQueryWrapper<FileTrashDetail> wrapper = new LambdaQueryWrapper<FileTrashDetail>().in(FileTrashDetail::getTrashId, trash.getId());
            List<FileTrashDetail> detailList = fileTrashDetailService.list(wrapper);
            List<Long> fileIds = detailList.stream().map(FileTrashDetail::getFileId).toList();
            fileService.recycle(fileIds, trash.getFileId());
            fileTrashDetailService.delete(detailList.stream().map(FileTrashDetail::getId).toList());
            LambdaUpdateWrapper<FileTrashDetail> updateDetailWrapper = new LambdaUpdateWrapper<>();
            updateDetailWrapper.set(FileTrashDetail::getDeleted, 1).in(FileTrashDetail::getId, detailList.stream().map(FileTrashDetail::getId).toList());
            fileTrashDetailService.update(updateDetailWrapper);
        });
        LambdaUpdateWrapper<FileTrash> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(FileTrash::getDeleted, 1).in(FileTrash::getId, list.stream().map(FileTrash::getId).toList());
        update(updateWrapper);
    }

    public List<FileTrash> getExpireList(){
        List<FileTrash> list = list(new LambdaQueryWrapper<FileTrash>().le(FileTrash::getExpireTime, System.currentTimeMillis()));
        return list;
    }

}

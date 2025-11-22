package cn.lxinet.lfs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lxinet.lfs.convert.TransProgressConvert;
import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.TransProgress;
import cn.lxinet.lfs.enums.FileTransStatus;
import cn.lxinet.lfs.mapper.TransProgressMapper;
import cn.lxinet.lfs.vo.FileVo;
import cn.lxinet.lfs.vo.TransProgressVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 转码进度
 *
 * @author zcx
 * @date 2023/11/27
 */
@Service
public class TransProgressService extends ServiceImpl<TransProgressMapper, TransProgress> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransProgressService.class);
    @Autowired
    private TransProgressMapper transProgressMapper;
    @Autowired
    @Lazy
    private FileService fileService;

    public List<TransProgress> queryByFileId(Long fileId){
        return transProgressMapper.queryByFileId(fileId);
    }

    /**
     * 更新转码进度
     *
     * @param id
     * @param progress
     */
    public void updateProgress(Long id, Double progress, Long fileTransId, boolean start){
        UpdateWrapper<TransProgress> wrapper = new UpdateWrapper();
        wrapper.set("progress", progress).set("file_trans_id", fileTransId).set("update_time", new Date());
        if (start){
            wrapper.set("start_time", System.currentTimeMillis());
        }
        if (progress == 100){
            wrapper.set("end_time", System.currentTimeMillis());
        }
        wrapper.eq("id", id);
        update(wrapper);
    }

    /**
     * 更新转码状态
     *
     * @param id
     * @param transStatus
     */
    public void updateTrans(Long id, Integer transStatus){
        TransProgress progress = getById(id);
        if (progress == null){
            return;
        }
        transProgressMapper.updateTrans(id, transStatus, new Date());
        List<TransProgress> progressList = queryByFileId(progress.getFileId());
        int size = progressList.size();
        int successNum = (int) progressList.stream().filter(p -> FileTransStatus.TRANS_SUCCESS.getStatus().equals(p.getTransStatus())).count();
        int failNum = (int) progressList.stream().filter(p -> FileTransStatus.TRANS_FAIL.getStatus().equals(p.getTransStatus())).count();
        Integer fileTransStatus = FileTransStatus.TRANS.getStatus();
        if (successNum == size){
            fileTransStatus = FileTransStatus.TRANS_SUCCESS.getStatus();
        }else if (failNum == size){
            fileTransStatus = FileTransStatus.TRANS_FAIL.getStatus();
        }else if ((successNum + failNum) == size){
            fileTransStatus = FileTransStatus.PART_TRANS_SUCCESS.getStatus();
        }
        fileService.updateTrans(progress.getFileId(), fileTransStatus);
    }


    public void deleteByFileId(Long fileId){
        deleteByFileIds(Collections.singletonList(fileId));
    }

    public void deleteByFileIds(List<Long> fileIds){
        UpdateWrapper<TransProgress> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("deleted", 1).in("file_id", fileIds);
        update(updateWrapper);
    }


    /**
     * 分页查询进度列表
     *
     * @param current 当前页码
     * @param size    每页数量
     * @return {@link Page}<{@link FileVo}>
     */
    public Page<TransProgressVo> listByPage(long current, long size){
        Page<TransProgress> page = page(new Page(current, size), new QueryWrapper<TransProgress>().orderByDesc("id"));
        Page<TransProgressVo> voPage = new Page<>(current, size, page.getTotal());
        if (page.getRecords().isEmpty()){
            return voPage;
        }
        List<TransProgressVo> voList = TransProgressConvert.INSTANCE.toVoList(page.getRecords());
        List<Long> fildIds = voList.stream().map(TransProgressVo::getFileId).collect(Collectors.toList());
        List<File> fileList = fileService.list(new QueryWrapper<File>().select("id", "name").in("id", fildIds));
        Map<Long, File> fileMap = new HashMap<>();
        fileList.forEach(file -> fileMap.put(file.getId(), file));
        voList.forEach(vo -> vo.setFileName(fileMap.get(vo.getFileId()) == null ? "" : fileMap.get(vo.getFileId()).getName()));
        voPage.setRecords(voList);
        return voPage;
    }

}

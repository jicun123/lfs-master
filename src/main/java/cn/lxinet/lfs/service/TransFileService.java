package cn.lxinet.lfs.service;

import cn.lxinet.lfs.entity.FileThum;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lxinet.lfs.entity.TransFile;
import cn.lxinet.lfs.mapper.TransFileMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 转码文件
 *
 * @author zcx
 * @date 2023/12/15
 */
@Service
public class TransFileService extends ServiceImpl<TransFileMapper, TransFile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransFileService.class);
    @Autowired
    private TransFileMapper transFileMapper;

    public void updateMd5(Long id, String md5){
        transFileMapper.updateMd5(id, md5, new Date());
    }

    public void deleteByFileId(Long fileId){
        deleteByFileIds(Collections.singletonList(fileId));
    }

    public void deleteByFileIds(List<Long> fileIds){
        UpdateWrapper<TransFile> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("deleted", 1).in("file_id", fileIds);
        update(updateWrapper);
    }

    public List<TransFile> listByFileIdWithDel(Long fileId){
        return transFileMapper.listByFileIdWithDel(fileId);
    }

}

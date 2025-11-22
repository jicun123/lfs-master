package cn.lxinet.lfs.service;

import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.FileTrashDetail;
import cn.lxinet.lfs.mapper.FileTrashDetailMapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文件回收站明细
 *
 * @author zcx
 * @date 2024/03/19
 */
@Slf4j
@Service
public class FileTrashDetailService extends ServiceImpl<FileTrashDetailMapper, FileTrashDetail> {
    @Autowired
    private FileTrashDetailMapper fileTrashDetailMapper;

    public void delete(List<Long> ids){
        LambdaUpdateWrapper<FileTrashDetail> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(FileTrashDetail::getDeleted, 1).in(FileTrashDetail::getId, ids);
        update(updateWrapper);
    }

}

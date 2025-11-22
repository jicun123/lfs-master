package cn.lxinet.lfs.listener;

import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.entity.FileThum;
import cn.lxinet.lfs.entity.TransFile;
import cn.lxinet.lfs.enums.EventTransType;
import cn.lxinet.lfs.enums.FileTransStatus;
import cn.lxinet.lfs.event.FileMd5Event;
import cn.lxinet.lfs.event.TransCompleteEvent;
import cn.lxinet.lfs.event.TransProgressEvent;
import cn.lxinet.lfs.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 转码完成监听
 *
 * @author zcx
 * @date 2023/11/20
 */
@Component
public class TransCompleteListener {
    private static Logger LOGGER = LoggerFactory.getLogger(TransCompleteListener.class);
    @Autowired
    private FileService fileService;
    @Autowired
    private TransFileService transFileService;
    @Autowired
    private FileThumService fileThumService;
    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private ApplicationContext applicationContext;

    @EventListener
    @Async
    public void transcodeComplete(TransCompleteEvent event) {
        //转码完成，更新数据信息
        if (event.getType() == EventTransType.TO_MP4
                || event.getType() == EventTransType.TO_HLS
                || event.getType() == EventTransType.TO_PDF){
            String absolutePath = event.getFilePath() == null ? "" : event.getFilePath();
            if (event.getTransStatus() != FileTransStatus.TRANS_SUCCESS){
                applicationContext.publishEvent(new TransProgressEvent(this, event.getTransProgressId(), event.getTransStatus().getStatus()));
                return;
            }
            String suffix = absolutePath.contains(".") ? absolutePath.substring(absolutePath.lastIndexOf(".")) : "";
            java.io.File file = new java.io.File(StringUtils.isNotBlank(event.getMp4FilePath()) ? event.getMp4FilePath() : absolutePath);
            //在windows系统下，保存数据库文件路径，要把\改成/
            String filePath = absolutePath.replace(fileConfig.getLocalFileDir(), "").replace("\\", "/");
            TransFile fileTrans = new TransFile(event.getFileId(), file.length(), suffix, filePath);
            transFileService.save(fileTrans);
            applicationContext.publishEvent(new TransProgressEvent(this, event.getTransProgressId(), 100d, fileTrans.getId(), event.getTransStatus().getStatus()));
            //目前只有转码文件才会计算md5，后续如果需要文件计算md5，这边需要调整，增加fileId设置
            FileMd5Event md5Event = new FileMd5Event(this, absolutePath, 0L, fileTrans.getId());
            applicationContext.publishEvent(md5Event);
            if (StringUtils.isNotBlank(event.getMp4FilePath())){
                file.delete();
            }
        }else if (event.getType() == EventTransType.GEN_VIDEO || event.getType() == EventTransType.GEN_PDF){
            List<Long> secondList = event.getSecondList();
            if (secondList.isEmpty()){
                return;
            }
            List<String> thumList = event.getThumList();
            List<FileThum> fileThumList = new ArrayList<>(thumList.size());
            for (int i = 0; i < secondList.size(); i ++){
                //在windows系统下，保存数据库文件路径，要把\改成/
                String thumPath = thumList.get(i).replace(fileConfig.getLocalFileDir(), "").replace("\\", "/");
                fileThumList.add(new FileThum(event.getMd5(), thumPath, secondList.get(i)));
            }
            fileThumService.saveBatch(fileThumList);
            //默认第一张图为缩略图
            fileService.updateFileThum(event.getFileId(), fileThumList.get(0).getPath());
        }
    }
}

package cn.lxinet.lfs.listener;

import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.TransProgress;
import cn.lxinet.lfs.entity.TransTemplate;
import cn.lxinet.lfs.enums.EventTransType;
import cn.lxinet.lfs.enums.FileTransStatus;
import cn.lxinet.lfs.enums.FileType;
import cn.lxinet.lfs.event.TransEvent;
import cn.lxinet.lfs.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 转码监听
 *
 * @author zcx
 * @date 2023/11/20
 */
@Component
public class TransListener {
    private static Logger LOGGER = LoggerFactory.getLogger(TransListener.class);
    @Autowired
    private FileService fileService;
    @Autowired
    private TransTemplateService transTemplateService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private TransProgressService transProgressService;
    @Autowired
    private DocumentService documentService;

    @EventListener
    @Async
    public void transcode(TransEvent event) {
        File file = fileService.getById(event.getFileId());
        if (file == null){
            LOGGER.info("文件转码开始，数据库记录不存在，取消转码，fileId：{}", event.getFileId());
            return;
        }
        if (event.getFileType() == FileType.VIDEO){
            if (event.getType() == EventTransType.TO_MP4 || event.getType() == EventTransType.TO_HLS){
                //视频转码
                List<TransTemplate> templateList = transTemplateService.queryOpenList();
                if (templateList.isEmpty()){
                    LOGGER.info("视频转码开始，视频转码开启的模板数量为空，取消转码，fileId：{}", event.getFileId());
                    fileService.updateTrans(file.getId(), FileTransStatus.TRANS_CANCEL.getStatus());
                    return;
                }
                //先保存需要转码的进度记录
                Map<Long, Long> progressIdMap = new HashMap<>();
                templateList.forEach(template -> {
                    TransProgress transProgress = new TransProgress(event.getFileId(), template.getFormat());
                    transProgressService.save(transProgress);
                    progressIdMap.put(template.getId(), transProgress.getId());
                });
                templateList.forEach(template -> videoService.transcode(event.getFileId(), event.getFilePath(), template, progressIdMap.get(template.getId())));
            }else if (event.getType() == EventTransType.GEN_VIDEO){
                videoService.videoCreateThum(event.getFileId(), event.getMd5(), event.getDuration(), event.getFilePath());
            }
        }else if (event.getFileType() == FileType.DOCUMENT){
            if (event.getType() == EventTransType.TO_PDF){
                //文档转码，文档转码无法监听转码进度，开始转码时，设置转码进度30%，完成后设置100%
                TransProgress transProgress = new TransProgress(event.getFileId(), "pdf", 30d, System.currentTimeMillis());
                transProgressService.save(transProgress);
                documentService.transcode(event.getFileId(), event.getMd5(), event.getFilePath(), transProgress.getId());
            }else if (event.getType() == EventTransType.GEN_PDF){
                documentService.genThumAndPages(event.getFileId(), event.getMd5(), event.getFilePath());
            }

        }
    }

}

package cn.lxinet.lfs.listener;

import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.TransFile;
import cn.lxinet.lfs.event.FileMd5Event;
import cn.lxinet.lfs.service.DocumentService;
import cn.lxinet.lfs.service.FileService;
import cn.lxinet.lfs.service.TransFileService;
import cn.lxinet.lfs.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 文件MD5计算
 *
 * @author zcx
 * @date 2023/11/22
 */
@Component
public class FileMd5Listener {
    private static Logger LOGGER = LoggerFactory.getLogger(FileMd5Listener.class);
    @Autowired
    private FileService fileService;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private TransFileService transFileService;

    @EventListener
    @Async
    public void md5(FileMd5Event event) {
        LOGGER.info("文件计算md5值，开始，fileId：{}，fileTransId：{}", event.getFileId(), event.getFileTransId());
        long s = System.currentTimeMillis();
        String md5 = FileUtil.calcMD5(new java.io.File(event.getFilePath()));
        if (event.getFileId() != null && event.getFileId() > 0){
            File file = fileService.getById(event.getFileId());
            if (file == null){
                return;
            }
            fileService.updateMd5(event.getFileId(), md5);
        }else if (event.getFileTransId() != null && event.getFileTransId() > 0){
            TransFile fileTrans = transFileService.getById(event.getFileTransId());
            if (fileTrans == null){
                return;
            }
            transFileService.updateMd5(event.getFileTransId(), md5);
        }
        long e = System.currentTimeMillis();
        LOGGER.info("文件计算md5值，结束，fileId：{}，fileTransId：{}，总耗时：{}", event.getFileId(), event.getFileTransId(), (e - s) / 1000 + "s");
    }

}

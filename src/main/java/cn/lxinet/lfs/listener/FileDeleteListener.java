package cn.lxinet.lfs.listener;

import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.entity.FileThum;
import cn.lxinet.lfs.entity.TransFile;
import cn.lxinet.lfs.event.FileDeleteEvent;
import cn.lxinet.lfs.event.FileMd5Event;
import cn.lxinet.lfs.service.DocumentService;
import cn.lxinet.lfs.service.FileService;
import cn.lxinet.lfs.service.FileThumService;
import cn.lxinet.lfs.service.TransFileService;
import cn.lxinet.lfs.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件MD5计算
 *
 * @author zcx
 * @date 2023/11/22
 */
@Component
public class FileDeleteListener {
    private static Logger LOGGER = LoggerFactory.getLogger(FileDeleteListener.class);
    @Autowired
    private FileService fileService;
    @Autowired
    private FileThumService fileThumService;
    @Autowired
    private TransFileService transFileService;
    @Autowired
    private FileConfig fileConfig;
    @EventListener
    @Async
    public void delete(FileDeleteEvent event) {
        event.getFileIds().forEach(fileId -> {
            LOGGER.info("文件删除，开始，fileId：{}", fileId);
            long s = System.currentTimeMillis();
            File file = fileService.getFileWithDel(fileId);
            if (file == null){
                LOGGER.info("文件删除，fileId找不到数据，取消删除，fileId：{}", fileId);
                return;
            }
            File md5File = fileService.findByMd5WithTrash(file.getMd5());
            if (md5File != null){
                LOGGER.info("文件删除，md5还存在其他文件，取消删除，fileId：{}", fileId);
                return;
            }
            deleteThums(fileId, file.getMd5());
            deleteTransFiles(fileId);
            deleteFile(fileId, file);
            long e = System.currentTimeMillis();
            LOGGER.info("文件删除，结束，fileId：{}，总耗时：{}", fileId, (e - s) / 1000 + "s");
        });
    }

    private void deleteFile(Long fileId, File file) {
        java.io.File diskFile = new java.io.File(fileConfig.getLocalFileDir() + file.getPath());
        if (diskFile.exists()){
            boolean res = diskFile.delete();
            if (res){
                LOGGER.info("文件删除，成功，fileId：{}", fileId);
            }else {
                LOGGER.info("文件删除，失败，fileId：{}", fileId);
            }
        }
    }

    private void deleteTransFiles(Long fileId) {
        LOGGER.info("文件删除，删除转码文件，开始，fileId：{}", fileId);
        List<TransFile> transList = transFileService.listByFileIdWithDel(fileId);
        if (!transList.isEmpty()){
            transList.forEach(trans -> {
                java.io.File diskTrans = new java.io.File(fileConfig.getLocalFileDir() + trans.getPath());
                if (diskTrans.exists()){
                    boolean res = diskTrans.delete();
                    if (res){
                        LOGGER.info("文件删除，删除转码文件，成功，fileId：{}", fileId);
                    }else {
                        LOGGER.info("文件删除，删除转码文件，失败，fileId：{}", fileId);
                    }
                }
            });
        }
        LOGGER.info("文件删除，删除转码文件，结束，fileId：{}", fileId);
    }

    private void deleteThums(Long fileId, String fileMd5) {
        LOGGER.info("文件删除，删除缩略图，开始，fileId：{}", fileId);
        List<FileThum> thumList = fileThumService.listByFileMd5WithDel(fileMd5);
        if (!thumList.isEmpty()){
            thumList.forEach(thum -> {
                java.io.File diskThum = new java.io.File(fileConfig.getLocalFileDir() + thum.getPath());
                if (diskThum.exists()){
                    boolean res = diskThum.delete();
                    if (res){
                        LOGGER.info("文件删除，删除缩略图，成功，fileId：{}", fileId);
                    }else {
                        LOGGER.info("文件删除，删除缩略图，失败，fileId：{}", fileId);
                    }
                }
            });
        }
        LOGGER.info("文件删除，删除缩略图，结束，fileId：{}", fileId);
    }

}

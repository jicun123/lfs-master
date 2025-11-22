package cn.lxinet.lfs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.config.PdfWatermarkConfig;
import cn.lxinet.lfs.entity.FileThum;
import cn.lxinet.lfs.enums.EventTransType;
import cn.lxinet.lfs.enums.FileTransStatus;
import cn.lxinet.lfs.enums.FileType;
import cn.lxinet.lfs.event.TransCompleteEvent;
import cn.lxinet.lfs.event.TransEvent;
import cn.lxinet.lfs.utils.DocumentUtil;
import cn.lxinet.lfs.utils.FileUtil;
import cn.lxinet.lfs.utils.PdfUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);
    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileThumService fileThumService;
    @Autowired
    private PdfWatermarkConfig pdfWatermarkConfig;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 文档转码
     */
    @Async("transcodeTaskExecutor")
    public void transcode(Long fileId, String fileMd5, String sourcePath, Long transProgressId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LOGGER.info("文档转码开始，fileId：{}", fileId);
        File sourceFile = new File(sourcePath);
        TransCompleteEvent completeEvent = new TransCompleteEvent(this, fileId, transProgressId, EventTransType.TO_PDF);
        if (!sourceFile.exists()){
            LOGGER.info("文档转码开始，本地文件不存在，取消转码，fileId：{}", fileId);
            fileService.updateTrans(fileId, FileTransStatus.TRANS_FAIL.getStatus());
            return;
        }
        try {
            String suffix = sourcePath.substring(sourcePath.lastIndexOf("."));
            String targetPath = fileConfig.getDocumentTransPath();
            if (FileUtil.isWord(suffix) || FileUtil.isWps(suffix) || FileUtil.isTxt(suffix) || FileUtil.isRtf(suffix)){
                DocumentUtil.word2Pdf(sourcePath, targetPath);
            }else if (FileUtil.isExcel(suffix)){
                DocumentUtil.excel2Pdf(sourcePath, targetPath);
            }else if (FileUtil.isPpt(suffix)){
                DocumentUtil.ppt2Pdf(sourcePath, targetPath);
            }else if (FileUtil.isPdf(suffix)){
                //pdf不需要转码
                fileService.updateTrans(fileId, FileTransStatus.NO_NEED_TRANS.getStatus());
                return;
            }
//            pdfWatermark(fileId, targetPath, targetPath);
            completeEvent.setTransStatus(FileTransStatus.TRANS_SUCCESS);
            completeEvent.setFilePath(targetPath);
            //生成封面，使用原文件md5
            applicationContext.publishEvent(new TransEvent(this, fileId, EventTransType.GEN_PDF, FileType.DOCUMENT, targetPath, fileMd5));
            stopWatch.stop();
            LOGGER.info("文档转码结束，fileId：{}，总耗时：{}", fileId, stopWatch.getTime() / 1000 + "s");
        } catch (Exception e) {
            completeEvent.setTransStatus(FileTransStatus.TRANS_FAIL);
            LOGGER.info("文档转码出现异常，fileId：" + fileId, e);
        }
        applicationContext.publishEvent(completeEvent);
    }


    /**
     * pdf生成封面和页数
     * @return
     * @throws Exception
     */
    @Async("transcodeTaskExecutor")
    public void genThumAndPages(Long fileId, String md5, String sourcePath) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.info("pdf生成封面和页数开始，md5：{}", md5);
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()){
                LOGGER.info("pdf生成封面和页数，本地文件不存在，取消生成，md5：{}", md5);
                return;
            }
            PDDocument doc = PdfUtil.getPDDocument(sourcePath);
            //更新页数
            fileService.updateDuration(fileId, (long) doc.getNumberOfPages());
            LOGGER.info("pdf生成页数结束，md5：{}", md5);
            List<FileThum> fileThumList = fileThumService.list(new QueryWrapper<FileThum>().eq("file_md5", md5));
            if (!fileThumList.isEmpty()){
                LOGGER.info("pdf生成封面和页数，数据库已存在封面，取消生成，md5：{}", md5);
                fileService.updateFileThum(fileId, fileThumList.get(0).getPath());
                return;
            }
            List<String> thumPathList = new ArrayList<>();
            List<Long> pageList = new ArrayList<>();
            PdfUtil.createThum(doc, fileConfig.getThumDir(md5), pageList, thumPathList);
            TransCompleteEvent completeEvent = new TransCompleteEvent(this, fileId, md5, thumPathList, pageList, EventTransType.GEN_PDF);
            applicationContext.publishEvent(completeEvent);
            stopWatch.stop();
            LOGGER.info("pdf生成封面和页数结束，md5：{}，总耗时：{}", md5, stopWatch.getTime() / 1000 + "s");
        } catch (Exception e) {
            LOGGER.error("pdf生成封面和页数异常，md5：" + md5, e);
        }
    }

    /**
     * pdf添加水印
     *
     * @param fileId
     * @param sourcePath
     * @param targetPath
     */
    public void pdfWatermark(Long fileId, String sourcePath, String targetPath) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.info("pdf添加水印开始，fileId：{}", fileId);
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()){
                LOGGER.info("pdf添加水印开始，本地文件不存在，取消生成，md5：{}", fileId);
                return;
            }
            PdfUtil.pdfWatermark(sourcePath, targetPath);
            stopWatch.stop();
            LOGGER.info("pdf添加水印开始，fileId：{}，总耗时：{}", fileId, stopWatch.getTime() / 1000 + "s");
        } catch (Exception e) {
            LOGGER.error("pdf添加水印开始出现异常，fileId：" + fileId, e);
        }
    }

}

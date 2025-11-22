package cn.lxinet.lfs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.entity.FileThum;
import cn.lxinet.lfs.entity.TransTemplate;
import cn.lxinet.lfs.enums.EventTransType;
import cn.lxinet.lfs.enums.FileTransStatus;
import cn.lxinet.lfs.event.TransCompleteEvent;
import cn.lxinet.lfs.utils.VideoUtil;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class VideoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoService.class);
    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private FileThumService fileThumService;
    @Autowired
    @Lazy
    private FileService fileService;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 视频转码
     * @return
     * @throws Exception
     */
    @Async("transcodeTaskExecutor")
    public void transcode(Long videoId, String sourcePath, TransTemplate template, Long transProgressId) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        LOGGER.info("视频转码开始，fileId：{}", videoId);
        File sourceFile = new File(sourcePath);
        TransCompleteEvent completeEvent = new TransCompleteEvent(this, videoId, transProgressId, EventTransType.TO_MP4);
        if (!sourceFile.exists()){
            LOGGER.info("视频转码开始，本地文件不存在，取消转码，fileId：{}", videoId);
            completeEvent.setTransStatus(FileTransStatus.TRANS_FAIL);
            completeEvent.setMessage("本地文件不存在");
            applicationContext.publishEvent(completeEvent);
            return;
        }
        try {
            String targetPath = fileConfig.getVideoTransPath();
            VideoUtil.toMp4(transProgressId, sourcePath, targetPath, template);
            if ("m3u8".equalsIgnoreCase(template.getFormat())){
                video2hls(videoId, targetPath, transProgressId);
            }else {
                completeEvent.setTransStatus(FileTransStatus.TRANS_SUCCESS);
                completeEvent.setFilePath(targetPath);
                applicationContext.publishEvent(completeEvent);
            }
            stopWatch.stop();
            LOGGER.info("视频转码结束，fileId：{}，总耗时：{}", videoId, stopWatch.getTime() / 1000 + "s");
        } catch (Exception e) {
            completeEvent.setTransStatus(FileTransStatus.TRANS_FAIL);
            completeEvent.setMessage("转码出现异常");
            applicationContext.publishEvent(completeEvent);
            LOGGER.info("视频转码出现异常，fileId：" + videoId, e);
        }
    }


    /**
     * 视频转hls
     * @return
     * @throws Exception
     */
    public boolean video2hls(Long videoId, String sourcePath, Long transProgressId) {
        TransCompleteEvent completeEvent = new TransCompleteEvent(this, videoId, transProgressId, EventTransType.TO_HLS);
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.info("视频转hls开始，fileId：{}", videoId);
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()){
                LOGGER.info("视频转码开始，本地文件不存在，取消转码，fileId：{}", videoId);
                completeEvent.setTransStatus(FileTransStatus.TRANS_FAIL);
                completeEvent.setMessage("本地文件不存在");
                return false;
            }
            String uuid = UUID.randomUUID().toString();
            String targetPath = fileConfig.getVideoHlsPath(uuid);
            VideoUtil.toHls(sourcePath, targetPath);
            completeEvent.setTransStatus(FileTransStatus.TRANS_SUCCESS);
            completeEvent.setTransProgressId(transProgressId);
            completeEvent.setFilePath(targetPath);
            completeEvent.setMp4FilePath(sourcePath);
            stopWatch.stop();
            LOGGER.info("视频转hls结束，fileId：{}，总耗时：{}", videoId, stopWatch.getTime() / 1000 + "s");
        } catch (Exception e) {
            completeEvent.setTransStatus(FileTransStatus.TRANS_FAIL);
            completeEvent.setMessage("转hls出现异常");
            LOGGER.error("视频转hls出现异常，fileId：" + videoId, e);
            return false;
        }finally {
            applicationContext.publishEvent(completeEvent);
        }
        return true;
    }

    /**
     * 视频生成封面
     * @return
     * @throws Exception
     */
    public void videoCreateThum(Long fileId, String md5, Long duration, String sourcePath) {
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            LOGGER.info("视频生成封面开始，md5：{}", md5);
            if (duration == 0){
                LOGGER.info("视频生成封面开始，视频总时长为0秒，取消生成，md5：{}", md5);
                return;
            }
            List<FileThum> fileThumList = fileThumService.list(new QueryWrapper<FileThum>().eq("file_md5", md5));
            if (!fileThumList.isEmpty()){
                LOGGER.info("视频生成封面开始，数据库已存在封面，取消生成，md5：{}", md5);
                fileService.updateFileThum(fileId, fileThumList.get(0).getPath());
                return;
            }
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()){
                LOGGER.info("视频生成封面开始，本地文件不存在，取消生成，md5：{}", md5);
                return;
            }
            List<Long> secondList = getScreenSecondList(duration);
            List<String> thumPathList = new ArrayList<>();
            secondList.forEach(second -> thumPathList.add(fileConfig.getThumPath(md5, second)));
            VideoUtil.videoScreenshot(sourcePath, thumPathList, secondList);
            TransCompleteEvent completeEvent = new TransCompleteEvent(this, fileId, md5, thumPathList, secondList, EventTransType.GEN_VIDEO);
            applicationContext.publishEvent(completeEvent);
            stopWatch.stop();
            LOGGER.info("视频生成封面结束，md5：{}，总耗时：{}", md5, stopWatch.getTime() / 1000 + "s");
        } catch (Exception e) {
            LOGGER.error("视频生成封面出现异常，md5：" + md5, e);
        }
    }

    private List<Long> getScreenSecondList(Long duration){
        if (duration <= 10){
            List<Long> secondList = new ArrayList<>();
            for (long i = 1; i <= duration; i ++){
                secondList.add(i);
            }
            if (secondList.isEmpty()){
                secondList.add(0L);
            }
            return secondList;
        }
        HashSet<Long> secondList = new HashSet<>();
        secondList.add(1L);
        secondList.add(3L);
        secondList.add(5L);
        secondList.add(7L);
        Long halfSecond = duration / 2;
        secondList.add(halfSecond);
        secondList.add(halfSecond + 2);
        secondList.add(duration - 7);
        secondList.add(duration - 5);
        secondList.add(duration - 3);
        secondList.add(duration - 1);
        return new ArrayList<>(secondList);
    }

}

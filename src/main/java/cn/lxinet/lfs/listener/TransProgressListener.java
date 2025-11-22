package cn.lxinet.lfs.listener;

import cn.lxinet.lfs.event.TransProgressEvent;
import cn.lxinet.lfs.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 转码进度监听
 *
 * @author zcx
 * @date 2023/11/27
 */
@Component
public class TransProgressListener {
    private static Logger LOGGER = LoggerFactory.getLogger(TransProgressListener.class);
    @Autowired
    private TransProgressService transProgressService;

    @EventListener
    @Async
    public void progress(TransProgressEvent event) {
        Long fileTransId = event.getFileTransId() == null ? 0L : event.getFileTransId();
        if (event.getProgress() != null){
            double progress = event.getProgress();
            //防止进度虽然100了，但是转码文件还未入库，在这个时间差内访问文件可能造成播放异常
            //在这个时间段内，强制改成进度99.9%
            if (fileTransId == 0 && progress == 100){
                progress = 99.9;
            }
            transProgressService.updateProgress(event.getTransProgressId(), progress, fileTransId, event.isStart());
        }
        if (event.getTransStatus() != null){
            transProgressService.updateTrans(event.getTransProgressId(), event.getTransStatus());
        }
    }

}

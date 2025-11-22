package cn.lxinet.lfs.listener;

import com.alibaba.fastjson2.JSONObject;
import cn.lxinet.lfs.event.TransProgressEvent;
import cn.lxinet.lfs.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.progress.EncoderProgressListener;

/**
 * 视频转码进度监听
 *
 * @author zcx
 * @date 2023/11/27
 */
public class ProgressListener implements EncoderProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressListener.class);
    private Long transProgressId;
    private Long lastUpdateDbTime = 0L;

    public ProgressListener(Long transProgressId){
        this.transProgressId = transProgressId;
    }

    @Override
    public void sourceInfo(MultimediaInfo multimediaInfo) {
        LOGGER.info("视频转mp4开始，transProgressId：{}，原视频信息：{}", transProgressId, JSONObject.toJSONString(multimediaInfo));
    }

    @Override
    public void progress(int i) {
        boolean start = lastUpdateDbTime == 0;
        //进度每500毫秒同步一次，每3秒钟更新数据库，避免频繁更新数据库造成数据库压力过大
        if (System.currentTimeMillis() - lastUpdateDbTime < 3000){
            return;
        }
        //更新进度
        LOGGER.info("视频转mp4开始，transProgressId：{}，进度：{}", transProgressId, i / 10d);
        SpringContextUtil.getApplicationContext().publishEvent(new TransProgressEvent(this, transProgressId, i / 10d, start));
        lastUpdateDbTime = System.currentTimeMillis();
    }

    @Override
    public void message(String s) {
        LOGGER.info("视频转mp4开始，transProgressId：{}，转码消息：{}", transProgressId, s);
    }
}

package cn.lxinet.lfs.task;

import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.service.RedisService;
import cn.lxinet.lfs.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 文件定时处理任务
 *
 * @author zcx
 * @date 2023/11/20
 */
@Component
public class FileTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileTask.class);
    @Autowired
    private RedisService redisService;
    @Autowired
    private FileConfig fileConfig;

    /**
     * 定时删除上传文件生成的临时文件夹
     * 正常上传过程中临时文件夹会自动删除
     * 该方法处理异常场景遗留的临时文件，如：上传一半关闭浏览器等各种异常场景
     * 每小时48分12秒执行一次
     */
    @Scheduled(cron = "12 48 0/1 * * ?")
    public void delTempFile() {
        try {
            LOGGER.info("定时删除上传文件生成的临时文件夹，开始");
            File file = new File(fileConfig.getUploadTempDir(""));
            if (!file.exists()){
                return;
            }
            String[] dirList = file.list();
            for (String name : dirList){
                //缓存24个小时，超过24小时的临时文件夹直接删除
                if (!redisService.exists(fileConfig.getUploadIdKey(name))){
                    LOGGER.info("定时删除上传文件生成的临时文件夹，开始删除文件夹：{}", file.getAbsolutePath() + File.separator + name);
                    FileUtil.delDir(new File(file.getAbsolutePath() + File.separator + name));
                }
            }
            LOGGER.info("定时删除上传文件生成的临时文件夹，结束");
        } catch (Exception e) {
            LOGGER.error("定时删除上传文件生成的临时文件夹出现异常：", e);
        }
    }

}

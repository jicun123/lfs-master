package cn.lxinet.lfs.task;

import cn.lxinet.lfs.entity.FileTrash;
import cn.lxinet.lfs.service.FileTrashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 回收站定时处理任务
 *
 * @author zcx
 * @date 2024/03/31
 */
@Component
public class TrashTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(TrashTask.class);
    @Autowired
    private FileTrashService fileTrashService;

    /**
     * 定时删除已过期的回收站文件
     */
    @Scheduled(cron = "40 13 2 * * ?")
    public void delExpireFile() {
        try {
            LOGGER.info("定时删除已过期的回收站文件，开始");
            List<FileTrash> expireList = fileTrashService.getExpireList();
            if (expireList.isEmpty()){
                LOGGER.info("定时删除已过期的回收站文件，不存在已过期的回收站文件");
                return;
            }
            List<String> ids = expireList.stream().map(fileTrash -> String.valueOf(fileTrash.getId())).toList();
            LOGGER.info("定时删除已过期的回收站文件，过期文件数量：{}", ids.size());
            fileTrashService.delete(ids);
            LOGGER.info("定时删除已过期的回收站文件，结束");
        } catch (Exception e) {
            LOGGER.error("定时删除已过期的回收站文件出现异常：", e);
        }
    }

}

package cn.lxinet.lfs.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * 文件删除事件
 *
 * @author zcx
 * @date 2024/03/24
 */
@Setter
@Getter
public class FileDeleteEvent extends ApplicationEvent {
    private List<Long> fileIds;

    public FileDeleteEvent(Object source, List<Long> fileIds) {
        super(source);
        this.fileIds = fileIds;
    }

}

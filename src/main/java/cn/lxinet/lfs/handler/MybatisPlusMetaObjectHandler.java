package cn.lxinet.lfs.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Objects;

@Component
public class MybatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的填充策略
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        Object createTime = this.getFieldValByName("createTime", metaObject);
        Object updateTime = this.getFieldValByName("updateTime", metaObject);
        //如果没手动设置时间值，就自动填充当前时间,如果有手动设置值，以设置值为准
        if(Objects.isNull(createTime)){
            this.setFieldValByName("createTime", new Date(), metaObject);
        }
        if(Objects.isNull(updateTime)){
            this.setFieldValByName("updateTime", new Date(), metaObject);
        }
    }

    /**
     * 修改时的填充策略
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateTime", new Date(), metaObject);
    }

}

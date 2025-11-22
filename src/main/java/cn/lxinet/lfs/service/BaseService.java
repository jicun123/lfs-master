package cn.lxinet.lfs.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public class BaseService<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    public boolean update(Object id, String column, Object value){
        return update(new UpdateWrapper<T>().set(column, value).eq("id", id));
    }

    public boolean update(Object id, String[] columns, Object[] values){
        UpdateWrapper<T> wrapper = new UpdateWrapper<>();
        for (int i = 0; i < columns.length; i ++){
            wrapper.set(columns[i], values[i]);
        }
        wrapper.eq("id", id);
        return update(wrapper);
    }

}

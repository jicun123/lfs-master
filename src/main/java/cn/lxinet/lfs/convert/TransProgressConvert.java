package cn.lxinet.lfs.convert;

import cn.lxinet.lfs.entity.TransProgress;
import cn.lxinet.lfs.vo.TransProgressVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文件对象转换
 *
 * @author zcx
 * @date 2023/11/27
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransProgressConvert {
    TransProgressConvert INSTANCE = Mappers.getMapper(TransProgressConvert.class);

    TransProgressVo toVo(TransProgress transProgress);

    List<TransProgressVo> toVoList(List<TransProgress> list);

}
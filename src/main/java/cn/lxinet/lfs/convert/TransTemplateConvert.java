package cn.lxinet.lfs.convert;

import cn.lxinet.lfs.dto.TransTemplateDto;
import cn.lxinet.lfs.entity.TransTemplate;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * 转码模板对象转换
 *
 * @author zcx
 * @date 2023/11/26
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransTemplateConvert{
    TransTemplateConvert INSTANCE = Mappers.getMapper(TransTemplateConvert.class);

    TransTemplate toEntity(TransTemplateDto dto);

}
package cn.lxinet.lfs.convert;

import cn.lxinet.lfs.entity.FileThum;
import cn.lxinet.lfs.vo.FileThumVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文件缩略图对象转换
 *
 * @author zcx
 * @date 2023/11/25
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileThumConvert {
    FileThumConvert INSTANCE = Mappers.getMapper(FileThumConvert.class);

    FileThumVo toVo(FileThum fileThum);

    List<FileThumVo> toVoList(List<FileThum> list);

}
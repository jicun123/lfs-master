package cn.lxinet.lfs.convert;

import cn.lxinet.lfs.entity.File;
import cn.lxinet.lfs.vo.FileVo;
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
public interface FileConvert {
    FileConvert INSTANCE = Mappers.getMapper(FileConvert.class);

    FileVo toVo(File file);

    List<FileVo> toVoList(List<File> list);

}
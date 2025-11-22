package cn.lxinet.lfs.convert;

import cn.lxinet.lfs.entity.FileTrash;
import cn.lxinet.lfs.vo.FileTrashVo;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 文件回收站对象转换
 *
 * @author zcx
 * @date 2024/03/16
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileTrashConvert {
    FileTrashConvert INSTANCE = Mappers.getMapper(FileTrashConvert.class);

    FileTrashVo toVo(FileTrash fileTrash);

    List<FileTrashVo> toVoList(List<FileTrash> list);

}
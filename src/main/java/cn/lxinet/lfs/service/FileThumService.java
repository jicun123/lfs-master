package cn.lxinet.lfs.service;

import cn.lxinet.lfs.entity.File;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.convert.FileThumConvert;
import cn.lxinet.lfs.entity.FileThum;
import cn.lxinet.lfs.mapper.FileThumMapper;
import cn.lxinet.lfs.vo.FileThumVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文件缩略图
 *
 * @author zcx
 * @date 2023/11/25
 */
@Service
public class FileThumService extends ServiceImpl<FileThumMapper, FileThum> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileThumService.class);
    @Autowired
    private FileThumMapper fileThumMapper;
    @Autowired
    private FileConfig fileConfig;

    public List<FileThumVo> listByMd5(String md5){
        QueryWrapper<FileThum> wrapper = new QueryWrapper<>();
        wrapper.select("id", "path", "duration").eq("file_md5", md5);
        List<FileThum> list = list(wrapper);
        List<FileThumVo> voList = FileThumConvert.INSTANCE.toVoList(list);
        voList.forEach(vo -> vo.setFileUrl(fileConfig.getPreviewUrl(vo.getPath())));
        return voList;
    }

    public List<FileThum> listByFileMd5WithDel(String fileMd5){
        return fileThumMapper.listByFileMd5WithDel(fileMd5);
    }

}

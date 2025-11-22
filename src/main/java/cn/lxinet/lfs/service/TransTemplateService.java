package cn.lxinet.lfs.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.lxinet.lfs.convert.TransTemplateConvert;
import cn.lxinet.lfs.dto.TransTemplateDto;
import cn.lxinet.lfs.entity.TransTemplate;
import cn.lxinet.lfs.mapper.TransTemplateMapper;
import cn.lxinet.lfs.message.ErrorCode;
import cn.lxinet.lfs.utils.Assert;
import cn.lxinet.lfs.utils.VideoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 转码模板
 *
 * @author zcx
 * @date 2023/11/26
 */
@Service
public class TransTemplateService extends ServiceImpl<TransTemplateMapper, TransTemplate> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransTemplateService.class);
    @Autowired
    private TransTemplateMapper transTemplateMapper;

    public List<TransTemplate> queryOpenList(){
        return transTemplateMapper.queryOpenList();
    }

    public Long save(TransTemplateDto dto) {
        validate(dto);
        TransTemplate transTemplate = TransTemplateConvert.INSTANCE.toEntity(dto);
        super.save(transTemplate);
        return transTemplate.getId();
    }

    public void update(TransTemplateDto dto) {
        validate(dto);
        Assert.notNull(getById(dto.getId()), ErrorCode.VIDEO_TRANS_TEMPLATE_NOT_EXIST);
        updateById(TransTemplateConvert.INSTANCE.toEntity(dto));
    }

    private void validate(TransTemplateDto dto){
        Assert.isTrue(VideoUtil.FORMAT.contains(dto.getFormat().toLowerCase()), ErrorCode.PARAM_ERROR, "转码输出格式参数错误");
        Assert.isTrue(VideoUtil.FRAME_RATE.contains(dto.getFrameRate()), ErrorCode.PARAM_ERROR, "视频帧率参数错误");
        Assert.isTrue(VideoUtil.BIT_RATE.contains(dto.getBitRate()), ErrorCode.PARAM_ERROR, "视频比特率参数错误");
        Assert.isTrue(VideoUtil.CODEC.contains(dto.getCodec().toLowerCase()), ErrorCode.PARAM_ERROR, "视频编解码器参数错误");
        Assert.isTrue(VideoUtil.AUDIO_CODEC.contains(dto.getAudioCodec().toLowerCase()), ErrorCode.PARAM_ERROR, "音频编解码器参数错误");
        Assert.isTrue(VideoUtil.AUDIO_CHANNEL.contains(dto.getAudioChannel()), ErrorCode.PARAM_ERROR, "音频声道参数错误");
        Assert.isTrue(VideoUtil.AUDIO_BIT_RATE.contains(dto.getAudioBitRate()), ErrorCode.PARAM_ERROR, "音频比特率参数错误");
        Assert.isTrue(VideoUtil.AUDIO_SAMPLE_RATE.contains(dto.getAudioSampleRate()), ErrorCode.PARAM_ERROR, "音频采样率参数错误");
    }

    /**
     * 获取所有模板最后一次更新的更新时间
     *
     * @return {@link Date}
     */
    public Date getLastUpdatedTime(){
        QueryWrapper<TransTemplate> wrapper = new QueryWrapper<>();
        wrapper.select("id", "update_time").orderByDesc("update_time");
        TransTemplate transTemplate = getOne(wrapper, false);
        return transTemplate == null ? null : transTemplate.getUpdateTime();
    }

    /**
     * 更新状态，开启/关闭模板
     *
     * @param id
     * @param status
     */
    public void updateStatus(Long id, Integer status){
        Assert.isTrue(status == 0 || status == 1, ErrorCode.PARAM_ERROR);
        Assert.notNull(getById(id), ErrorCode.VIDEO_TRANS_TEMPLATE_NOT_EXIST);
        transTemplateMapper.updateStatus(id, status, new Date());
    }

    /**
     * 删除
     *
     * @param id
     */
    public void delete(Long id){
        Assert.notNull(id, ErrorCode.PARAM_ERROR);
        update(new UpdateWrapper<TransTemplate>().set("deleted", 1).set("update_time", new Date()).eq("id", id));
    }
}

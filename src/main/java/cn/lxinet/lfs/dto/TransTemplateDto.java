package cn.lxinet.lfs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;

/**
 * 转码模板
 *
 * @author zcx
 * @date 2023/11/25
 */
@Data
public class TransTemplateDto implements Serializable {
    private Long id;
    @NotBlank(message = "模板名称不能为空")
    @Length(min = 1, max = 255, message = "模板名称长度需要在{min}-{max}之间")
    private String name;
    @NotNull(message = "视频分辨率宽度不能为空")
    @Range(min = 0, max = 10000, message = "视频分辨率宽度只能在{min}-{max}之间")
    private Integer width;
    @NotNull(message = "视频分辨率高度不能为空")
    @Range(min = 0, max = 10000, message = "视频分辨率高度只能在{min}-{max}之间")
    private Integer height;
    @NotNull(message = "视频输出格式不能为空")
    private String format;
    @NotNull(message = "视频帧率不能为空")
    @Range(min = 15, max = 60, message = "视频帧率只能在{min}-{max}之间")
    private Integer frameRate;
    @NotNull(message = "视频比特率不能为空")
    @Range(min = 500, max = 8000, message = "视频比特率只能在{min}-{max}之间")
    private Integer bitRate;
    @NotNull(message = "视频编解码器不能为空")
    private String codec;
    @NotNull(message = "音频编解码器不能为空")
    private String audioCodec;
    @NotNull(message = "音频声道不能为空")
    @Range(min = 1, max = 2, message = "音频声道只能在{min}-{max}之间")
    private Integer audioChannel;
    @NotNull(message = "音频比特率不能为空")
    @Range(min = 16, max = 512, message = "音频比特率只能在{min}-{max}之间")
    private Integer audioBitRate;
    @NotNull(message = "音频采样率不能为空")
    @Range(min = 8000, max = 96000, message = "音频采样率只能在{min}-{max}之间")
    private Integer audioSampleRate;
    @NotNull(message = "状态不能为空")
    @Range(min = 0, max = 1, message = "状态只能在{min}-{max}之间")
    private Integer status;

}

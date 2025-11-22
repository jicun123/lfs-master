package cn.lxinet.lfs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 * 视频水印配置
 *
 * @author zcx
 * @date 2023/11/26
 */
@Configuration
@ConfigurationProperties("config.video.watermark")
public class VideoWatermarkConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoWatermarkConfig.class);
    private static final List<String> LOCATION_LIST = Arrays.asList("TOP_LEFT", "TOP_RIGHT", "BOTTOM_RIGHT", "BOTTOM_LEFT");
    private static File wmFile;
    private boolean enable;
    private String location;
    private Integer offsetX;
    private Integer offsetY;

    public VideoWatermarkConfig INSTANCE(){
        try {
            wmFile = ResourceUtils.getFile("classpath:static/watermark.png");
        } catch (FileNotFoundException e) {
            LOGGER.error("视频水印文件不存在，无法开启视频水印功能，classpath:static/watermark.png");
            setEnable(false);
            return this;
        }
        if (!LOCATION_LIST.contains(location)){
            LOGGER.error("视频水印location设置错误，无法开启视频水印，当前设置：{}，必须为以下四个其中一个：{}", location, LOCATION_LIST.toArray());
            setEnable(false);
            return this;
        }
        return this;
    }

    public VideoWatermarkConfig() {

    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Boolean getEnable() {
        return enable;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(Integer offsetX) {
        this.offsetX = offsetX;
    }

    public Integer getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(Integer offsetY) {
        this.offsetY = offsetY;
    }

    public File getWmFile() {
        return wmFile;
    }
}

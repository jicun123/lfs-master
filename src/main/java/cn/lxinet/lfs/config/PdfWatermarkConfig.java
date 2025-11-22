package cn.lxinet.lfs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * pdf水印配置
 *
 * @author zcx
 * @date 2023/11/26
 */
@Configuration
@ConfigurationProperties("config.document.watermark")
public class PdfWatermarkConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfWatermarkConfig.class);
    private boolean enable;
    private boolean timeEnable;
    private String timeFormat;
    private String content;
    private Integer fontSize;
    private Integer rowSpace;
    private Integer colSpace;
    private Float alpha;

    public PdfWatermarkConfig INSTANCE(){
        if (timeEnable){
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
                sdf.format(new Date());
            } catch (Exception e) {
                LOGGER.error("时间格式设置错误，无法开启文档水印功能，当前设置为：{}", timeFormat);
                setEnable(false);
                return this;
            }
        }
        if (alpha < 0 || alpha > 1){
            LOGGER.error("alpha只能设置为0-1的小数，无法开启文档水印功能，当前设置为：{}", alpha);
            setEnable(false);
            return this;
        }
        return this;
    }

    public PdfWatermarkConfig() {

    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isTimeEnable() {
        return timeEnable;
    }

    public void setTimeEnable(boolean timeEnable) {
        this.timeEnable = timeEnable;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public Integer getRowSpace() {
        return rowSpace;
    }

    public void setRowSpace(Integer rowSpace) {
        this.rowSpace = rowSpace;
    }

    public Integer getColSpace() {
        return colSpace;
    }

    public void setColSpace(Integer colSpace) {
        this.colSpace = colSpace;
    }

    public Float getAlpha() {
        return alpha;
    }

    public void setAlpha(Float alpha) {
        this.alpha = alpha;
    }
}

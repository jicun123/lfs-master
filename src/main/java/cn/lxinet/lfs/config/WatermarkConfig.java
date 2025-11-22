package cn.lxinet.lfs.config;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文字水印配置
 *
 * @author zcx
 * @date 2023/11/24
 */
@Data
@Configuration
@ConfigurationProperties("config.document.watermark")
public class WatermarkConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(WatermarkConfig.class);
    private boolean enable;
    private boolean timeEnable;
    private String timeFormat;
    private String content;
    private Integer fontSize;
    private Integer rowSpace;
    private Integer colSpace;
    private Float alpha;

    public WatermarkConfig INSTANCE(){
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

    public WatermarkConfig() {

    }

}

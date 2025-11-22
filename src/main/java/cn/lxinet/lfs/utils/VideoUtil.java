package cn.lxinet.lfs.utils;

import cn.lxinet.lfs.config.VideoWatermarkConfig;
import cn.lxinet.lfs.entity.TransTemplate;
import cn.lxinet.lfs.listener.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.ScreenExtractor;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.filtergraphs.OverlayWatermark;
import ws.schild.jave.filters.helpers.OverlayLocation;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.info.VideoSize;
import ws.schild.jave.process.ProcessWrapper;
import ws.schild.jave.process.ffmpeg.DefaultFFMPEGLocator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 视频工具
 *
 * @author zcx
 * @date 2023/11/22
 */
public class VideoUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(VideoUtil.class);
    /**
     * 转码输出格式
     */
    public static final List<String> FORMAT = Arrays.asList("mp4", "m3u8");
    /**
     * 视频帧率
     */
    public static final List<Integer> FRAME_RATE = Arrays.asList(15, 20, 25, 30, 40, 50, 60);
    /**
     * 视频比特率(kbps)
     */
    public static final List<Integer> BIT_RATE = Arrays.asList(500, 800, 1200, 2000, 3000, 5000, 8000);
    /**
     * 编解码器
     */
    public static final List<String> CODEC = Arrays.asList("h264");
    /**
     * 音频编解码器
     */
    public static final List<String> AUDIO_CODEC = Arrays.asList("aac");
    /**
     * 音频声道
     */
    public static final List<Integer> AUDIO_CHANNEL = Arrays.asList(1, 2);
    /**
     * 音频比特率(kbps)
     */
    public static final List<Integer> AUDIO_BIT_RATE = Arrays.asList(16, 32, 48, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320, 384, 448, 512);
    /**
     * 音频采样率(Hz)
     */
    public static final List<Integer> AUDIO_SAMPLE_RATE = Arrays.asList(8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000);

    /**
     * 获取视频信息
     * @param sourcePath
     * @return
     */
    public static MultimediaInfo getVideoInfo(String sourcePath) {
        MultimediaInfo multimediaInfo = null;
        try {
            multimediaInfo = new MultimediaObject(new File(sourcePath)).getInfo();
        } catch (EncoderException e) {
            LOGGER.error("获取视频信息出现异常：", e);
        }
        return multimediaInfo;
    }

    /**
     * 视频转mp4
     * @param transProgressId
     * @param sourcePath
     * @param targetPath
     * @param template
     * @throws EncoderException
     */
    public static void toMp4(Long transProgressId, String sourcePath, String targetPath, TransTemplate template) throws Exception {
        File sourceFile = new File(sourcePath);
        File targetFile = new File(targetPath);
        MultimediaObject multimediaObject = new MultimediaObject(sourceFile);
        EncodingAttributes attributes = new EncodingAttributes();
        setAudioAttributes(attributes, template);
        setVideoAttributes(multimediaObject, attributes, template);
        attributes.setOutputFormat("mp4");
        Encoder encoder = new Encoder();
        encoder.encode(multimediaObject, targetFile, attributes, new ProgressListener(transProgressId));
    }

    /**
     * 视频转hls
     * @param sourcePath
     * @param targetPath
     * @throws Exception
     */
    public static void toHls(String sourcePath, String targetPath) throws Exception {
        ProcessWrapper command = new DefaultFFMPEGLocator().createExecutor();
        command.addArgument("-i");
        command.addArgument(sourcePath);
        command.addArgument("-c:v");
        command.addArgument("copy");
        command.addArgument("-c:a");
        command.addArgument("copy");
        command.addArgument("-f");
        command.addArgument("ssegment");
        command.addArgument("-segment_format");
        command.addArgument("mpegts");
        command.addArgument("-segment_list");
        command.addArgument(targetPath);
        command.addArgument("-segment_time");
        command.addArgument("10");
        command.addArgument(targetPath.replace(".m3u8", "_%d.ts"));
        command.execute();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(command.getErrorStream()))) {
            blockFfmpeg(br);
        }
    }

    /**
     * 视频截图
     * @param sourcePath
     * @param targetPathList 截图文件路径列表
     * @param secondsList 截取的秒 列表 eg:[1,10,20] 截取 第1秒，第10秒，第20秒
     * @throws EncoderException
     */
    public static void videoScreenshot(String sourcePath, List<String> targetPathList, List<Long> secondsList) throws EncoderException {
        //默认截取第1秒
        if (secondsList == null || secondsList.isEmpty()) {
            secondsList = new ArrayList<>();
            secondsList.add(1L);
        }
        MultimediaObject multimediaObject = new MultimediaObject(new File(sourcePath));
        ScreenExtractor screenExtractor = new ScreenExtractor();
        for (int i = 0; i < secondsList.size(); i ++){
            String targetPath = targetPathList.get(i);
            File targetFile = new File(targetPath);
            screenExtractor.renderOneImage(multimediaObject, -1, -1, secondsList.get(i) * 1000, targetFile, 1);
        }
    }

    /**
     * 设置视频的音频参数
     * @param attributes
     */
    private static void setAudioAttributes(EncodingAttributes attributes, TransTemplate template){
        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setCodec(template.getAudioCodec());
        audioAttributes.setBitRate(template.getAudioBitRate() * 1024);
        audioAttributes.setSamplingRate(template.getAudioSampleRate());
        audioAttributes.setChannels(template.getAudioChannel());
        attributes.setAudioAttributes(audioAttributes);
    }

    /**
     * 获取视频分辨率
     * @param multimediaObject
     * @return
     */
    private static VideoSize getVideoSize(MultimediaObject multimediaObject) throws EncoderException {
        VideoSize videoSize = multimediaObject.getInfo().getVideo().getSize();
        return videoSize;
    }

    /**
     * 设置视频的视频参数
     * @param multimediaObject
     * @param attributes
     */
    private static void setVideoAttributes(MultimediaObject multimediaObject, EncodingAttributes attributes, TransTemplate template) throws Exception {
        // 设置视频的视频参数
        VideoAttributes videoAttributes = new VideoAttributes();
        // 设置帧率
        videoAttributes.setFrameRate(template.getFrameRate());
        // 设置码率
        videoAttributes.setBitRate(template.getBitRate() * 1024);
        // 设置编解码器
        videoAttributes.setCodec(template.getCodec().toLowerCase());
        //设置水印
        setWatermark(videoAttributes);
        // 设置分辨率
        setVideoSize(multimediaObject, videoAttributes, template);
        attributes.setVideoAttributes(videoAttributes);
    }

    /**
     * 设置视频分辨率
     *
     * @param multimediaObject
     * @param videoAttributes
     * @param template
     * @throws EncoderException
     */
    private static void setVideoSize(MultimediaObject multimediaObject, VideoAttributes videoAttributes, TransTemplate template) throws EncoderException {
        VideoSize videoSize = getVideoSize(multimediaObject);
        int videoWidth = videoSize.getWidth();
        int videoHeight = videoSize.getHeight();
        int height = template.getHeight();
        int width = template.getWidth();
        if (width == 0 && height == 0){
            width = videoWidth;
            height = videoHeight;
        }else if (width > 0 && height == 0){
            height = videoHeight * width / videoWidth;
            //不支持奇数
            height = height % 2 == 0 ? height : height + 1;
        }else if (width == 0 && height > 0){
            width = videoWidth * height / videoHeight;
            //不支持奇数
            width = width % 2 == 0 ? width : width + 1;
        }
//        videoAttributes.setSize(new VideoSize(width, height));
        int finalWidth = width;
        int finalHeight = height;
        videoAttributes.addFilter(() -> "scale=" + finalWidth + ":" + finalHeight
//                + ",setsar=1:1"
        );
    }

    /**
     * 设置水印
     *
     * @param videoAttributes
     */
    private static void setWatermark(VideoAttributes videoAttributes) {
        VideoWatermarkConfig videoWatermarkConfig = SpringContextUtil.getBean(VideoWatermarkConfig.class).INSTANCE();
        if (!videoWatermarkConfig.getEnable()){
            LOGGER.info("视频添加水印已关闭，跳过添加水印");
            return;
        }
        OverlayLocation location = OverlayLocation.TOP_LEFT;
        Integer offsetX = videoWatermarkConfig.getOffsetX();
        Integer offsetY = videoWatermarkConfig.getOffsetY();
        if (OverlayLocation.TOP_LEFT.toString().equals(videoWatermarkConfig.getLocation())){
            location = OverlayLocation.TOP_LEFT;
        }else if (OverlayLocation.TOP_RIGHT.toString().equals(videoWatermarkConfig.getLocation())){
            location = OverlayLocation.TOP_RIGHT;
            offsetX = -offsetX;
        }else if (OverlayLocation.BOTTOM_LEFT.toString().equals(videoWatermarkConfig.getLocation())){
            location = OverlayLocation.BOTTOM_LEFT;
            offsetY = -offsetY;
        }else if (OverlayLocation.BOTTOM_RIGHT.toString().equals(videoWatermarkConfig.getLocation())){
            location = OverlayLocation.BOTTOM_RIGHT;
            offsetX = -offsetX;
            offsetY = -offsetY;
        }
        OverlayWatermark overlayWatermark = new OverlayWatermark(videoWatermarkConfig.getWmFile(), location, offsetX, offsetY);
        videoAttributes.addFilter(overlayWatermark);
    }

    /**
     * 等待命令执行成功后退出
     * @param br
     * @throws IOException
     */
    private static void blockFfmpeg(BufferedReader br) throws IOException {
        String line;
        // 该方法阻塞线程，直至处理完成
        while ((line = br.readLine()) != null) {}
    }
}

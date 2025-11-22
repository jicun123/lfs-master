package cn.lxinet.lfs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;

/**
 * 文件工具
 *
 * @author zcx
 * @date 2023/11/09
 */
public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);
    private static final List<String> VIDEO_SUFFIX = Arrays.asList(".mp4", ".avi", ".m4v", ".flv", ".mov", ".mpg", ".mpeg", ".mkv", ".rmvb", ".3gp", ".wmv", ".wma", ".rm");
    private static final List<String> AUDIO_SUFFIX = Arrays.asList(".mp3");
    private static final List<String> DOCUMENT_SUFFIX = Arrays.asList(".doc", ".docx", ".ppt", ".pptx", ".xls", ".xlsx", ".pdf", ".wps", ".txt", ".rtf");
    private static final List<String> IMAGE_SUFFIX = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");

    /**
     * 判断是否是视频
     * @param suffix
     * @return
     */
    public static boolean isVideo(String suffix){
        return suffix != null && VIDEO_SUFFIX.contains(suffix.toLowerCase());
    }
    /**
     * 判断是否是音频
     * @param suffix
     * @return
     */
    public static boolean isAudio(String suffix){
        return suffix != null && AUDIO_SUFFIX.contains(suffix.toLowerCase());
    }

    /**
     * 判断是否是文档
     * @param suffix
     * @return
     */
    public static boolean isDocument(String suffix){
        return suffix != null && DOCUMENT_SUFFIX.contains(suffix.toLowerCase());
    }

    /**
     * 判断是否是图片
     * @param suffix
     * @return
     */
    public static boolean isImage(String suffix){
        return suffix != null && IMAGE_SUFFIX.contains(suffix.toLowerCase());
    }

    /**
     * 判断是否是word
     * @param suffix
     * @return
     */
    public static boolean isWord(String suffix){
        return ".doc".equalsIgnoreCase(suffix) || ".docx".equalsIgnoreCase(suffix);
    }

    /**
     * 判断是否是wps
     * @param suffix
     * @return
     */
    public static boolean isWps(String suffix){
        return ".wps".equalsIgnoreCase(suffix);
    }

    /**
     * 判断是否是excel
     * @param suffix
     * @return
     */
    public static boolean isExcel(String suffix){
        return ".xls".equalsIgnoreCase(suffix) || ".xlsx".equalsIgnoreCase(suffix);
    }

    /**
     * 判断是否是ppt
     * @param suffix
     * @return
     */
    public static boolean isPpt(String suffix){
        return ".ppt".equalsIgnoreCase(suffix) || ".pptx".equalsIgnoreCase(suffix);
    }

    /**
     * 判断是否是ppt
     * @param suffix
     * @return
     */
    public static boolean isPdf(String suffix){
        return ".pdf".equalsIgnoreCase(suffix);
    }

    /**
     * 判断是否是txt
     * @param suffix
     * @return
     */
    public static boolean isTxt(String suffix){
        return ".txt".equalsIgnoreCase(suffix);
    }

    /**
     * 判断是否是rtf
     * @param suffix
     * @return
     */
    public static boolean isRtf(String suffix){
        return ".rtf".equalsIgnoreCase(suffix);
    }

    /**
     * 计算文件MD5值
     * @param file
     * @return
     */
    public static String calcMD5(File file) {
        try (InputStream stream = Files.newInputStream(file.toPath(), StandardOpenOption.READ)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[8192];
            int len;
            while ((len = stream.read(buf)) > 0) {
                digest.update(buf, 0, len);
            }
            return toHexString(digest.digest());
        } catch (Exception e) {
            LOGGER.error("计算文件MD5值出现异常：", e);
            return "";
        }
    }

    private static String toHexString(byte[] data) {
        char[] hexCode = "0123456789abcdef".toCharArray();
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(hexCode[(b >> 4) & 0xF]);
            r.append(hexCode[(b & 0xF)]);
        }
        return r.toString();
    }

    /**
     * 创建文件夹
     * @param path
     * @return
     */
    public static String mkDirs(String path){
        File dir = new File(path);
        mkDirs(dir);
        return path;
    }

    /**
     * 创建文件夹
     * @param dir
     */
    public static void mkDirs(File dir){
        if (!dir.exists()){
            dir.mkdirs();
        }
    }

    /**
     * 删除文件夹
     * @param dir
     */
    public static void delDir(File dir){
        if (dir.isDirectory()){
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i ++) {
                delDir(children[i]);
            }
        }
        dir.delete();
    }

}

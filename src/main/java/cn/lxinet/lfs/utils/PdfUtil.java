package cn.lxinet.lfs.utils;

import cn.lxinet.lfs.config.PdfWatermarkConfig;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PdfUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PdfUtil.class);
    private static InputStream fontIs = PdfUtil.class.getResourceAsStream("/static/MicrosoftYaHei.ttf");

    /**
     * pdf 生成缩略图
     * 主要是用做word、ppt、excel等文档转码后的pdf生成缩略图
     * 如果pdf小于10页，每页都生成图片
     * 如果pdf大于10页，生成前5张和后5张
     *
     * @param doc
     * @param targetDir      目标目录
     * @param targetPathList 目标路径列表
     */
    public static void createThum(PDDocument doc, String targetDir, List<Long> pageList, List<String> targetPathList) throws IOException {
        PDFRenderer renderer = new PDFRenderer(doc);
        int pageCount = doc.getNumberOfPages();
        if (pageCount == 0){
            return;
        }
        File dir = new File(targetDir);
        if (!dir.exists()){
            dir.mkdirs();
        }
        if (pageCount <= 10){
            for (int i = 0; i < pageCount; i ++){
                saveImg(renderer, targetDir, pageList, targetPathList, i);
            }
        }else {
            for (int i = 0; i < 5; i ++){
                saveImg(renderer, targetDir, pageList, targetPathList, i);
            }
            for (int i = pageCount - 5; i < pageCount; i ++){
                saveImg(renderer, targetDir, pageList, targetPathList, i);
            }
        }
    }

    public static PDDocument getPDDocument(String sourcePath) throws IOException {
        File pdf = new File(sourcePath);
        PDDocument doc = Loader.loadPDF(pdf);
        return doc;
    }

    public static void pdfWatermark(String sourcePath, String targetPath) throws Exception{
        PdfWatermarkConfig config = SpringContextUtil.getBean(PdfWatermarkConfig.class).INSTANCE();
        if (!config.isEnable()){
            LOGGER.info("pdf添加水印已关闭，跳过添加水印");
            return;
        }
        PDDocument document = Loader.loadPDF(new File(sourcePath));
        try {
            document.setAllSecurityToBeRemoved(true);
            // 遍历PDF文件，在每一页加上水印
            for (PDPage page : document.getPages()) {
                PDPageContentStream stream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);
                // 加载水印字体
                PDFont font = PDType0Font.load(document, fontIs, true);
                PDExtendedGraphicsState r = new PDExtendedGraphicsState();
                // 设置透明度
                r.setNonStrokingAlphaConstant(config.getAlpha());
                r.setAlphaSourceFlag(true);
                stream.setGraphicsStateParameters(r);
                stream.beginText();
                stream.setFont(font, config.getFontSize());
                stream.newLineAtOffset(0, -15);
                // 获取PDF页面大小
                float pageHeight = page.getMediaBox().getHeight();
                float pageWidth = page.getMediaBox().getWidth();
                // 根据纸张大小添加水印，30度倾斜
                for (int h = 10; h < pageHeight; h = h + config.getRowSpace()) {
                    for (int w = - 10; w < pageWidth; w = w + config.getColSpace()) {
                        stream.setTextMatrix(Matrix.getRotateInstance(0.3, w, h));
                        stream.showText(getWmContent(config));
                    }
                }
                // 结束渲染，关闭流
                stream.endText();
                stream.restoreGraphicsState();
                stream.close();
            }
            document.save(new File(targetPath));
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    private static void saveImg(PDFRenderer renderer, String targetDir, List<Long> pageList, List<String> targetPathList, Integer i) {
        try {
            BufferedImage thum = renderer.renderImage(i, 0.8f);
            String filePath = targetDir + File.separator + (i + 1) + ".jpg";
            ImageIO.write(thum, "jpg", new File(filePath));
            targetPathList.add(filePath);
            pageList.add(Long.valueOf(i + 1));
        }catch (Exception e){
            //如果找不到相关字体，会报错，先忽略不管
        }
    }


    public static String getWmContent(PdfWatermarkConfig config){
        String time = "";
        if (config.isTimeEnable()){
            time = new SimpleDateFormat(config.getTimeFormat()).format(new Date());
        }
        return config.getContent() + " " + time;
    }
}

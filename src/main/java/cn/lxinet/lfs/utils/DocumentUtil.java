package cn.lxinet.lfs.utils;

import com.aspose.cells.PdfSaveOptions;
import com.aspose.cells.Workbook;
import com.aspose.slides.Presentation;
import com.aspose.words.Document;
import com.aspose.words.ImportFormatMode;
import com.aspose.words.SaveFormat;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文档工具
 *
 * @author zcx
 * @date 2023/11/22
 */
public class DocumentUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentUtil.class);

    public static void main(String[] args) {
        String sourcePath = "/Users/zcx/develop/lxinet/test/《创新管理》读书笔记.pptx";
        String targetPath = "/Users/zcx/develop/lxinet/test/《创新管理》读书笔记.pdf";
        ppt2Pdf(sourcePath, targetPath);
    }
    public static void word2Pdf(String sourcePath, String targetPath) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        FileOutputStream os = null;
        try {
            File file = new File(targetPath);
            os = new FileOutputStream(file);
            Document doc = new Document(sourcePath);
            Document document = new Document();
            document.removeAllChildren();
            document.appendDocument(doc, ImportFormatMode.USE_DESTINATION_STYLES);
            document.save(os, SaveFormat.PDF);
        }catch (Exception e){
            LOGGER.error("word转pdf出现异常", e);
        }finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        stopWatch.stop();
        LOGGER.info("文档转码结束，总耗时：{}", stopWatch.getTime() / 1000 + "s");

    }

    public static void excel2Pdf(String sourcePath, String targetPath) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        FileOutputStream os = null;
        try {
            Workbook wb = new Workbook(sourcePath);
            os = new FileOutputStream(targetPath);
            PdfSaveOptions pdfSaveOptions = new PdfSaveOptions();
            pdfSaveOptions.setOnePagePerSheet(false);
            pdfSaveOptions.setAllColumnsInOnePagePerSheet(false);
            wb.save(os, pdfSaveOptions);
        } catch (Exception e) {
            LOGGER.error("excel转pdf出现异常", e);
        }finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        stopWatch.stop();
        LOGGER.info("文档转码结束，总耗时：{}", stopWatch.getTime() / 1000 + "s");
    }

    public static void ppt2Pdf(String sourcePath, String targetPath) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(sourcePath);
            os = new FileOutputStream(targetPath);
            Presentation pres = new Presentation(is);
            pres.save(os, com.aspose.slides.SaveFormat.Pdf);
        } catch (Exception e) {
            LOGGER.error("ppt转pdf出现异常", e);
        }finally {
            try {
                if (os != null) os.close();
                if (is != null) is.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        stopWatch.stop();
        LOGGER.info("文档转码结束，总耗时：{}", stopWatch.getTime() / 1000 + "s");
    }


}

package cn.lxinet.lfs.controller;

import cn.lxinet.lfs.config.FileConfig;
import cn.lxinet.lfs.service.SafetychainService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.web.servlet.HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE;
import static org.springframework.web.servlet.HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE;

/**
 * 本地文件预览/下载控制器
 *
 * @author GPT
 */
@RestController
@RequestMapping({"/fs", "/api/fs"})
public class FilePreviewController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilePreviewController.class);
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    private FileConfig fileConfig;
    @Autowired
    private SafetychainService safetychainService;

    @GetMapping("/**")
    public void handle(HttpServletRequest request,
                       HttpServletResponse response) throws IOException {
        String fullPath = (String) request.getAttribute(PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
        String relativePath = antPathMatcher.extractPathWithinPattern(bestMatchPattern, fullPath);

        String secret = request.getParameter("secret");
        String expireStr = request.getParameter("expire");
        String oper = request.getParameter("oper");
        String filename = request.getParameter("filename");

        if (StringUtils.isBlank(relativePath) || StringUtils.isBlank(secret) || StringUtils.isBlank(expireStr)) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return;
        }

        Long expire;
        try {
            expire = Long.parseLong(expireStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return;
        }

        String pathForValidation = relativePath.startsWith("/") ? relativePath : "/" + relativePath;

        if (!safetychainService.validate(pathForValidation, secret, expire)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        Path basePath = Paths.get(fileConfig.getLocalFileDir()).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(relativePath).normalize();
        if (!targetPath.startsWith(basePath)) {
            LOGGER.warn("非法访问尝试，path：{}", targetPath);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        if (!Files.exists(targetPath) || !Files.isReadable(targetPath)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        String contentType = Files.probeContentType(targetPath);
        if (StringUtils.isBlank(contentType)) {
            contentType = "application/octet-stream";
        }
        response.setContentType(contentType);

        if ("down".equalsIgnoreCase(oper)) {
            String downloadName = StringUtils.defaultIfBlank(filename, targetPath.getFileName().toString());
            String encoded = URLEncoder.encode(downloadName, StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
        }

        response.setHeader("Cache-Control", "private, max-age=60");

        try (ServletOutputStream outputStream = response.getOutputStream()) {
            Files.copy(targetPath, outputStream);
            outputStream.flush();
        }
    }
}


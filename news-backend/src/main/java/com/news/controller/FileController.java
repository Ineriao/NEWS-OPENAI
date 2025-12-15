package com.news.controller;

import com.news.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.max-size}")
    private long maxSize;

    // 允许的图片类型
    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    /**
     * 通用文件上传接口
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            String error = validateFile(file);
            if (error != null) {
                return Result.error(error);
            }

            // 保存文件
            String url = saveFile(file, "img");

            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", file.getOriginalFilename());

            logger.info("[文件上传] 上传成功: {}", url);
            return Result.success("上传成功", data);
        } catch (IOException e) {
            logger.error("[文件上传] 上传失败: {}", e.getMessage(), e);
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 富文本编辑器图片上传接口
     * POST /api/files/upload/editor
     * 返回格式符合 WangedEditor 规范
     */
    @PostMapping("/upload/editor")
    public Map<String, Object> uploadForEditor(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证文件
            String error = validateFile(file);
            if (error != null) {
                result.put("errno", 1);
                result.put("message", error);
                return result;
            }

            // 保存文件
            String url = saveFile(file, "editor");

            // WangedEditor 要求的返回格式
            result.put("errno", 0);
            Map<String, String> data = new HashMap<>();
            data.put("url", url);
            result.put("data", data);

            logger.info("[编辑器上传] 上传成功: {}", url);
        } catch (IOException e) {
            logger.error("[编辑器上传] 上传失败: {}", e.getMessage(), e);
            result.put("errno", 1);
            result.put("message", "文件上传失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 验证文件
     */
    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "请选择要上传的文件";
        }

        // 检查文件大小
        if (file.getSize() > maxSize) {
            return "文件大小超过限制（最大 " + (maxSize / 1024 / 1024) + "MB）";
        }

        // 检查文件类型
        String contentType = file.getContentType();
        boolean isAllowed = false;
        for (String type : ALLOWED_TYPES) {
            if (type.equals(contentType)) {
                isAllowed = true;
                break;
            }
        }
        if (!isAllowed) {
            return "不支持的文件类型，仅支持 JPG、PNG、GIF、WebP 格式";
        }

        return null;
    }

    /**
     * 保存文件到磁盘
     */
    private String saveFile(MultipartFile file, String prefix) throws IOException {
        // 生成日期目录 yyyy/MM/dd
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String dirPath = uploadPath + datePath;

        // 确保目录存在
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 生成文件名: prefix_uuid.ext
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = prefix + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ext;

        // 保存文件
        File destFile = new File(dir, filename);
        file.transferTo(destFile);

        // 返回访问 URL
        return "/uploads/" + datePath + "/" + filename;
    }
}

package com.agiantii.backend.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class FileStorageUtil {
    private static final String DEFAULT_UPLOAD_DIR = "uploads";
    // 20 MB
    public static final long MAX_FILE_SIZE = 20L * 1024L * 1024L;
    public static final List<String> ALLOWED_EXT = Arrays.asList("pdf", "doc", "docx", "zip", "png", "jpg", "jpeg", "txt");

    public static String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
        validateFile(file);
        String original = file.getOriginalFilename();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String filename = ts + "_" + (original == null ? "file" : original.replaceAll("\\s+", "_"));

        Path uploadDir = Paths.get(DEFAULT_UPLOAD_DIR);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path target = uploadDir.resolve(filename);
        file.transferTo(target.toFile());
        return target.toString().replace("\\\\", "/");
    }

    public static void validateFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return;
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File too large. Max allowed: " + MAX_FILE_SIZE + " bytes");
        }
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
            if (!ALLOWED_EXT.contains(ext)) {
                throw new IOException("File type not allowed: " + ext);
            }
        } else {
            throw new IOException("File has no extension");
        }
    }
}

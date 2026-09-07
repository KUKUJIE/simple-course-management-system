package com.agiantii.backend.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileStorageUtil {
    private static final String DEFAULT_UPLOAD_DIR = "uploads";

    public static String saveFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;
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
}

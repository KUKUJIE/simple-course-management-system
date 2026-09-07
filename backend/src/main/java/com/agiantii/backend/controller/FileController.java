package com.agiantii.backend.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @GetMapping("/download")
    public ResponseEntity<?> download(@RequestParam("path") String filePath) {
        // filePath is the server path saved earlier, relative or absolute
        try {
            Path p = Paths.get(filePath);
            if (!Files.exists(p)) {
                return ResponseEntity.notFound().build();
            }
            File f = p.toFile();
            byte[] data = FileCopyUtils.copyToByteArray(f);
            String filename = f.getName();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok().headers(headers).body(data);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to read file: " + e.getMessage());
        }
    }
}

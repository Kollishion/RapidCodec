package com.vid.compressor.service;

import java.io.File;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads/";

    public FileStorageService() {
        new File(uploadDir).mkdirs();
    }

    public String saveFile(MultipartFile file) throws IOException {
        String path = uploadDir + file.getOriginalFilename();
        File dest = new File(path);
        file.transferTo(dest);
        return path;
    }
}

package com.vid.compressor.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vid.compressor.model.EncodeMode;
import com.vid.compressor.model.EncodeResponse;

@Service
public class VideoService {

    private final FileStorageService fileStorage;
    private final EncoderService encoderService;

    public VideoService(FileStorageService fs, EncoderService es) {
        this.fileStorage = fs;
        this.encoderService = es;
    }

    public EncodeResponse processVideo(MultipartFile file, EncodeMode mode) throws Exception {
        String inputPath = fileStorage.saveFile(file);
        return encoderService.encode(inputPath, mode);
    }
}

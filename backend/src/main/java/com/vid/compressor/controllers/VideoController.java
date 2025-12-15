package com.vid.compressor.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vid.compressor.model.EncodeMode;
import com.vid.compressor.model.EncodeResponse;
import com.vid.compressor.service.VideoService;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoController {

    private final VideoService service;

    public VideoController(VideoService service) {
        this.service = service;
    }

    @PostMapping("/upload")
    public ResponseEntity<EncodeResponse> uploadVideo(
            @RequestPart MultipartFile file,
            @RequestParam EncodeMode mode) {

        try {
            EncodeResponse response = service.processVideo(file, mode);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

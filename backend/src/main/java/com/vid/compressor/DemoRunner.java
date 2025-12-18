package com.vid.compressor;

import java.io.File;

import com.vid.compressor.service.FullVideoDecoderService;
import com.vid.compressor.utils.VideoAssembler;

public class DemoRunner {

    public static void main(String[] args) throws Exception {

        File compressed = new File("output.bin");
        File decodedFrames = new File("decoded");
        File finalVideo = new File("decoded_output.mp4");

        FullVideoDecoderService decoder = new FullVideoDecoderService();

        decoder.decodeToFrames(
                compressed,
                30,      
                decodedFrames
        );

        VideoAssembler.assembleVideo(
                decodedFrames,
                finalVideo,         
                25
        );

        System.out.println("Decoding complete!");
        System.out.println("🎥 Video saved as: " + finalVideo.getAbsolutePath());
    }
}

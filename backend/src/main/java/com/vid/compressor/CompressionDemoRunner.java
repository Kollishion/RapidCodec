package com.vid.compressor;

import java.io.File;

import com.vid.compressor.encoders.BaselineEncoder;
import com.vid.compressor.encoders.Encoder;
import com.vid.compressor.encoders.ProposedEncoder2;
import com.vid.compressor.model.Metrics;

public class CompressionDemoRunner {

    public static void main(String[] args) throws Exception {

        // ================= MODE SELECTION =================
        // baseline | proposed2
        String mode = (args.length > 0) ? args[0].toLowerCase() : "proposed2";

        System.out.println("====================================");
        System.out.println(" RapidCodec Compression Demo");
        System.out.println(" Encoder Mode: " + mode.toUpperCase());
        System.out.println("====================================");

        // ================= PATHS =================
        File inputVideo   = new File("input/input.mp4");
        File framesFolder = new File("frames_raw");
        File outputFile   = new File(
                mode.equals("baseline")
                        ? "compressed_baseline.dat"
                        : "compressed_proposed2.dat"
        );

        if (!inputVideo.exists()) {
            throw new RuntimeException(
                    "Input video not found: " + inputVideo.getAbsolutePath()
            );
        }

        // ================= FRAME EXTRACTION =================
        if (!framesFolder.exists() || FrameReader.totalFrames(framesFolder) == 0) {
            System.out.println("Extracting frames...");
            FFmpegFrameExtractor.extract(inputVideo, framesFolder);
        } else {
            System.out.println("Frames already extracted. Skipping extraction.");
        }

        int totalFrames = FrameReader.totalFrames(framesFolder);
        if (totalFrames == 0) {
            throw new RuntimeException("No frames found in frames_raw");
        }

        System.out.println("Total frames: " + totalFrames);

        // ================= ENCODER SELECTION =================
        Encoder encoder;

        switch (mode) {
            case "baseline" -> encoder = new BaselineEncoder();
            case "proposed2" -> encoder = new ProposedEncoder2();
            default -> throw new IllegalArgumentException(
                    "Unknown mode: " + mode +
                    " (use baseline | proposed2)"
            );
        }

        // ================= ENCODE =================
        Metrics metrics = encoder.encode(
                framesFolder.getAbsolutePath(),
                outputFile.getAbsolutePath()
        );

        // ================= RESULTS =================
        System.out.println("\n===== ENCODING COMPLETE =====");
        System.out.println("Runtime           : " + metrics.getRuntimeMs() + " ms");
        System.out.println("Compression Ratio : " + metrics.getCompressionRatio());
        System.out.println("PSNR              : " + metrics.getPsnr());
        System.out.println("SSIM              : " + metrics.getSsim());
        System.out.println("Output file       : " + outputFile.getAbsolutePath());
    }
}

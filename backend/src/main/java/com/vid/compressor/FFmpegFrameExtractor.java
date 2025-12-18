package com.vid.compressor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class FFmpegFrameExtractor {
    private static final String FFMPEG = "ffmpeg";

    public static void extractFrames(String videoPath, String outputDir)
            throws IOException, InterruptedException {

        File dir = new File(outputDir);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create output directory");
            }
        }

        String outputPattern = outputDir + File.separator + "frame_%04d.png";

        ProcessBuilder pb = new ProcessBuilder(
            FFMPEG,
            "-i", videoPath,
            "-q:v", "4",
            "-vsync", "0",
            outputPattern
            );


        pb.inheritIO();
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed with exit code " + exitCode);
        }
    }
    public static void extract(File inputVideo, File outputDir) throws IOException, InterruptedException {

    if (!inputVideo.exists()) {
        throw new RuntimeException("Input video does not exist: " + inputVideo.getAbsolutePath());
    }

    if (!outputDir.exists()) outputDir.mkdirs();

    String outputPattern = outputDir.getAbsolutePath() + File.separator + "frame_%04d.png";

    ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-i", inputVideo.getAbsolutePath(),
            "-q:v", "4",
            "-vsync", "0",
            outputPattern
    );

    pb.redirectErrorStream(true); 
    Process p = pb.start();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line); 
        }
    }

    int exitCode = p.waitFor();
    System.out.println("FFmpeg exit code: " + exitCode);

    File[] files = outputDir.listFiles((d, name) -> name.endsWith(".png"));
    if (files == null || files.length == 0) {
        throw new RuntimeException("No PNG frames were created. Check FFmpeg logs above.");
    }

    System.out.println("Frames extracted: " + files.length);
}

}

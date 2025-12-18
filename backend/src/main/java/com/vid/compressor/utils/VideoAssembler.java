package com.vid.compressor.utils;

import java.io.File;
import java.io.IOException;

public class VideoAssembler {

    public static void assembleVideo(
            File framesDir,
            File outputVideo,
            int fps) throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-framerate", String.valueOf(fps),
                "-i", framesDir.getAbsolutePath() + "/frame_%d.png",
                "-c:v", "libx264",
                "-pix_fmt", "yuv420p",
                outputVideo.getAbsolutePath()
        );

        pb.inheritIO(); // shows ffmpeg logs
        Process p = pb.start();
        p.waitFor();
    }
}

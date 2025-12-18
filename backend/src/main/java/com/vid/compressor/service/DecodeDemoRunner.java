package com.vid.compressor.service;

import java.io.File;
import java.util.List;

import com.vid.compressor.decoder.FrameReconstructor;
import com.vid.compressor.decoder.FullVideoDecoder;
import com.vid.compressor.utils.VideoAssembler;

public class DecodeDemoRunner {

    public static void runDemo() throws Exception {

        File compressed = new File("output.bin");
        File framesDir = new File("decoded");
        framesDir.mkdirs();

        List<int[][]> frames =
                FullVideoDecoder.decode(compressed, 30);

        int idx = 0;
        for (int[][] frame : frames) {
            FrameReconstructor.writeFrame(
                    frame,
                    new File(framesDir, "frame_" + idx++ + ".png")
            );
        }

        VideoAssembler.assembleVideo(
                framesDir,
                new File("output_decoded.mp4"),
                25
        );

        System.out.println("✅ Video reconstructed successfully");
    }
}

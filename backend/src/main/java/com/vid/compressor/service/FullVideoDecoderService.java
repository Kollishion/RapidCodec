package com.vid.compressor.service;

import java.io.File;
import java.util.List;

import com.vid.compressor.decoder.FrameReconstructor;
import com.vid.compressor.decoder.FullVideoDecoder;

public class FullVideoDecoderService {

    public void decodeToFrames(
            File compressedFile,
            int frameCount,
            File outputDir) throws Exception {

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        List<int[][]> frames =
                FullVideoDecoder.decode(compressedFile, frameCount);

        int idx = 0;
        for (int[][] frame : frames) {
            FrameReconstructor.writeFrame(
                    frame,
                    new File(outputDir, "frame_" + idx++ + ".png")
            );
        }
    }
}

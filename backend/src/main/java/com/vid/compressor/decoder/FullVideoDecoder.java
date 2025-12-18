package com.vid.compressor.decoder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.vid.compressor.QuadtreeUtils;
import com.vid.compressor.entropy.HuffmanTable;

public class FullVideoDecoder {

    public static List<int[][]> decode(File compressed, int frameCount)
            throws IOException {

        List<int[][]> frames = new ArrayList<>();

        try (BitstreamReader reader = new BitstreamReader(compressed)) {

            HuffmanTable table = HuffmanHeaderReader.readHeader(reader);
            HuffmanBitReader huff = new HuffmanBitReader(reader, table);

            for (int i = 0; i < frameCount; i++) {
                int[][] frame = new int[
                        QuadtreeUtils.CTU_SIZE][QuadtreeUtils.CTU_SIZE];

                QuadtreeDecoder.decodeBlock(
                        0, 0,
                        QuadtreeUtils.CTU_SIZE,
                        frame,
                        huff
                );

                frames.add(frame);
            }
        }
        return frames;
    }
}

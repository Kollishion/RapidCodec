package com.vid.compressor.decoder;

import java.io.File;
import java.io.IOException;

import com.vid.compressor.decoder.BitstreamReader;
import com.vid.compressor.QuadtreeUtils;
import com.vid.compressor.entropy.HuffmanTable;

public class Decoder {

    public static int[][] decodeFrame(File input) throws IOException {

        try (BitstreamReader reader = new BitstreamReader(input)) {

            HuffmanTable table = HuffmanHeaderReader.readHeader(reader);
            HuffmanBitReader huff = new HuffmanBitReader(reader, table);

            int[][] frame = new int[
                    QuadtreeUtils.CTU_SIZE][QuadtreeUtils.CTU_SIZE];

            QuadtreeDecoder.decodeBlock(
                    0, 0,
                    QuadtreeUtils.CTU_SIZE,
                    frame,
                    huff
            );

            return frame;
        }
    }
}

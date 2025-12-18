package com.vid.compressor.decoder;

import java.io.IOException;

import com.vid.compressor.QuadtreeUtils;

public class QuadtreeDecoder {

    public static void decodeBlock(
            int sx, int sy, int size,
            int[][] frame,
            HuffmanBitReader reader) throws IOException {

        int split = reader.readSymbol();

        if (split == 1 && size > QuadtreeUtils.MIN_CU) {
            int h = size / 2;

            decodeBlock(sx, sy, h, frame, reader);
            decodeBlock(sx + h, sy, h, frame, reader);
            decodeBlock(sx, sy + h, h, frame, reader);
            decodeBlock(sx + h, sy + h, h, frame, reader);
        } else {
            int blockSize = reader.readSymbol();
            int avg = reader.readSymbol() - 10;

            for (int y = sy; y < sy + blockSize; y++) {
                for (int x = sx; x < sx + blockSize; x++) {
                    frame[y][x] = avg;
                }
            }
        }
    }
}

package com.vid.compressor.decoder;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class BitstreamReader implements Closeable {

    private final FileInputStream in;
    private int currentByte = 0;
    private int bitPos = 8;

    public BitstreamReader(File file) throws IOException {
        this.in = new FileInputStream(file);
    }

    public boolean readBit() throws IOException {
        if (bitPos == 8) {
            int val = in.read();
            if (val == -1) {
                throw new IOException("Unexpected EOF");
            }
            currentByte = val;
            bitPos = 0;
        }

        boolean bit = ((currentByte >> (7 - bitPos)) & 1) == 1;
        bitPos++;
        return bit;
    }
    public int readByte() throws IOException {
    int val = 0;
    for (int i = 0; i < 8; i++) {
        val = (val << 1) | (readBit() ? 1 : 0);
    }
    return val;
}

    @Override
    public void close() throws IOException {
        in.close();
    }
}

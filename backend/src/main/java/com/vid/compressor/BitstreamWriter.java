package com.vid.compressor;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitstreamWriter implements Closeable {

    private final OutputStream out;
    private int currentByte = 0;
    private int bitPos = 0;

    public BitstreamWriter(File file) throws IOException {
        this.out = new BufferedOutputStream(new FileOutputStream(file));
    }

    public void writeBit(boolean bit) throws IOException {
        if (bit) {
            currentByte |= (1 << (7 - bitPos));
        }
        bitPos++;

        if (bitPos == 8) {
            flushByte();
        }
    }

    public void writeByte(int value) throws IOException {
        flushPartial();
        out.write(value & 0xFF);
    }

    private void flushByte() throws IOException {
        out.write(currentByte);
        currentByte = 0;
        bitPos = 0;
    }

    private void flushPartial() throws IOException {
        if (bitPos > 0) {
            flushByte();
        }
    }

    @Override
    public void close() throws IOException {
        flushPartial();
        out.close();
    }
}

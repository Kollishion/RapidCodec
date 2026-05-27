package com.vid.compressor.decoder;

import java.io.IOException;

public class HuffmanDecoder {

    private final HuffmanBitReader reader;

    public HuffmanDecoder(HuffmanBitReader reader) {
        this.reader = reader;
    }

    public int readSymbol() throws IOException {
        return reader.readSymbol();
    }
}

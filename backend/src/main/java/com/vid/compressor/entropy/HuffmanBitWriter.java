package com.vid.compressor.entropy;

import java.io.IOException;
import java.util.Map;

import com.vid.compressor.BitstreamWriter;

public class HuffmanBitWriter {

    private final BitstreamWriter writer;
    private final Map<Integer, String> codes;

    public HuffmanBitWriter(BitstreamWriter writer, HuffmanTable table) {
        this.writer = writer;
        this.codes = table.getCodes();
    }

    public HuffmanBitWriter(BitstreamWriter writer, Map<Integer, String> codes) {
        this.writer = writer;
        this.codes = codes;
    }

   public void writeSymbol(int symbol) throws IOException {
    String code = codes.get(symbol);
    if (code == null) {
        throw new IllegalStateException(
            "No Huffman code for symbol: " + symbol);
    }

    for (int i = 0; i < code.length(); i++) {
        writer.writeBit(code.charAt(i) == '1');
    }
}


    public void flush() {

    }
}

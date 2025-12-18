package com.vid.compressor.decoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.vid.compressor.decoder.BitstreamReader;
import com.vid.compressor.entropy.HuffmanTable;

public class HuffmanBitReader {
    private final BitstreamReader reader;
    private final Map<String, Integer> reverseMap = new HashMap<>();
    private String currentBits = "";

    public HuffmanBitReader(BitstreamReader reader, HuffmanTable table) {
        this.reader = reader;
        table.getCodes().forEach((symbol, code) ->
                reverseMap.put(code, symbol));
    }

    public int readSymbol() throws IOException {
        while (true) {
            boolean bit = reader.readBit();
            currentBits += bit ? "1" : "0";

            Integer symbol = reverseMap.get(currentBits);
            if (symbol != null) {
                currentBits = "";
                return symbol;
            }
        }
    }
}

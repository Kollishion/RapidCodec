package com.vid.compressor.decoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.vid.compressor.entropy.HuffmanTable;

public class HuffmanHeaderReader {

    public static HuffmanTable readHeader(BitstreamReader reader)
            throws IOException {

        int count = reader.readByte();
        Map<Integer, String> codes = new HashMap<>();

        for (int i = 0; i < count; i++) {
            int symbol = reader.readByte();
            int len = reader.readByte();

            StringBuilder sb = new StringBuilder();
            for (int b = 0; b < len; b++) {
                sb.append(reader.readBit() ? '1' : '0');
            }
            codes.put(symbol, sb.toString());
        }

        HuffmanTable table = new HuffmanTable();
        table.getCodes().putAll(codes);
        return table;
    }
}

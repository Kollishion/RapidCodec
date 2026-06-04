package com.vid.compressor.decoder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.vid.compressor.entropy.HuffmanTable;

public class HuffmanHeaderReader {

    public static HuffmanTable readHeader(BitstreamReader reader)
            throws IOException {

        int count = reader.readBits(16);

        Map<Integer, String> codes = new HashMap<>();

        int symbol = 0;
        int len = 0;

        for (int i = 0; i < count; i++) {

            symbol = reader.readBits(16);
            len = reader.readBits(8);

            StringBuilder sb = new StringBuilder();

            for (int b = 0; b < len; b++) {
                sb.append(reader.readBit() ? '1' : '0');
            }

            codes.put(symbol, sb.toString());
        }

        reader.alignToByte();

        HuffmanTable table = new HuffmanTable();

        table.getCodes().putAll(codes);
        return table;
    }
}

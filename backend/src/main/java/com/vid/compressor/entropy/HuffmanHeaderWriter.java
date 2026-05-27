package com.vid.compressor.entropy;

import java.io.IOException;
import java.util.Map;

import com.vid.compressor.BitstreamWriter;

public class HuffmanHeaderWriter {

    public static void writeTable(
            BitstreamWriter writer,
            Map<Integer, String> table) throws IOException {

        writer.writeBits(table.size(), 8);

        for (var e : table.entrySet()) {
            int symbol = e.getKey();
            String code = e.getValue();

            writer.writeBits(symbol, 8);
            writer.writeBits(code.length(), 8);

            for (char c : code.toCharArray()) {
                writer.writeBit(c == '1');
            }
        }

        writer.flush();
    }

    public static void writeHeader(
            BitstreamWriter writer,
            HuffmanTable table) throws IOException {

        Map<Integer, String> codes = table.getCodes();

        writer.writeBits(codes.size(), 8);

        for (Map.Entry<Integer, String> e : codes.entrySet()) {
            int symbol = e.getKey();
            String code = e.getValue();

            writer.writeBits(symbol, 8);
            writer.writeBits(code.length(), 8);

            for (int i = 0; i < code.length(); i++) {
                writer.writeBit(code.charAt(i) == '1');
            }
        }

        writer.flush();
    }
}

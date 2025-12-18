package com.vid.compressor.entropy;

import java.io.IOException;
import java.util.Map;

import com.vid.compressor.BitstreamWriter;

public class HuffmanHeaderWriter {

    public static void writeTable(
            BitstreamWriter writer,
            Map<Integer, String> table) throws IOException {

        writer.writeByte(table.size());

        for (var e : table.entrySet()) {
            int symbol = e.getKey();
            String code = e.getValue();

            writer.writeByte(symbol);
            writer.writeByte(code.length());

            for (char c : code.toCharArray()) {
                writer.writeBit(c == '1');
            }
        }
    }
     public static void writeHeader(
            BitstreamWriter writer,
            HuffmanTable table) throws IOException {

        Map<Integer, String> codes = table.getCodes();

        writer.writeByte(codes.size());

        for (Map.Entry<Integer, String> e : codes.entrySet()) {
            int symbol = e.getKey();
            String code = e.getValue();

            writer.writeByte(symbol);

            writer.writeByte(code.length());

            for (int i = 0; i < code.length(); i++) {
                writer.writeBit(code.charAt(i) == '1');
            }
        }
    }
}

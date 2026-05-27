package com.vid.compressor.decoder;

import java.io.IOException;
import java.util.Map;

import com.vid.compressor.entropy.HuffmanTable;

public class HuffmanBitReader {

    private final BitstreamReader reader;
    private final Node root;

    private static class Node {
        Node zero;
        Node one;
        Integer symbol; 
    }

    public HuffmanBitReader(BitstreamReader reader, HuffmanTable table) {
        this.reader = reader;
        this.root = buildTrie(table.getCodes());
    }

    private Node buildTrie(Map<Integer, String> codes) {
        Node root = new Node();

        for (Map.Entry<Integer, String> entry : codes.entrySet()) {
            int symbol = entry.getKey();
            String code = entry.getValue();

            Node curr = root;
            for (char c : code.toCharArray()) {
                if (c == '0') {
                    if (curr.zero == null) curr.zero = new Node();
                    curr = curr.zero;
                } else {
                    if (curr.one == null) curr.one = new Node();
                    curr = curr.one;
                }
            }
            curr.symbol = symbol;
        }
        return root;
    }


    public int readSymbol() throws IOException {
        Node curr = root;

        while (curr.symbol == null) {
            boolean bit = reader.readBit();
            curr = bit ? curr.one : curr.zero;

            if (curr == null) {
                throw new IOException("Invalid Huffman code");
            }
        }
        return curr.symbol;
    }
}

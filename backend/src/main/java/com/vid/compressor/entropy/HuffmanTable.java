package com.vid.compressor.entropy;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanTable {

    private final Map<Integer, String> codes = new HashMap<>();

    public Map<Integer, String> getCodes() {
        return codes;
    }

    public static HuffmanTable build(int[] frequencies) {

        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();

        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                pq.add(new HuffmanNode(i, frequencies[i]));
            }
        }
        if (pq.size() == 1) {
            HuffmanNode only = pq.poll();
            HuffmanTable table = new HuffmanTable();
            table.codes.put(only.symbol, "0");
            return table;
        }

        while (pq.size() > 1) {
            HuffmanNode a = pq.poll();
            HuffmanNode b = pq.poll();
            pq.add(new HuffmanNode(a, b));
        }

        HuffmanNode root = pq.poll();
        HuffmanTable table = new HuffmanTable();
        buildCodes(root, "", table.codes);

        return table;
    }

    private static void buildCodes(
            HuffmanNode node,
            String prefix,
            Map<Integer, String> out) {

        if (node.isLeaf()) {
            out.put(node.symbol, prefix);
            return;
        }

        buildCodes(node.left, prefix + "0", out);
        buildCodes(node.right, prefix + "1", out);
    }
}

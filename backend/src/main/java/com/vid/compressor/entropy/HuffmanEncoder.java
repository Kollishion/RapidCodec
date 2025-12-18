package com.vid.compressor.entropy;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanEncoder {

    private final Map<Integer, String> codes = new HashMap<>();

    public Map<Integer, String> buildCodes(Map<Integer, Integer> freqMap) {

        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();

        for (var e : freqMap.entrySet()) {
            pq.add(new HuffmanNode(e.getKey(), e.getValue()));
        }

        while (pq.size() > 1) {
            HuffmanNode a = pq.poll();
            HuffmanNode b = pq.poll();
            pq.add(new HuffmanNode(a, b));
        }

        HuffmanNode root = pq.poll();
        buildCodeRecursive(root, "");

        return codes;
    }

    private void buildCodeRecursive(HuffmanNode node, String code) {
        if (node.isLeaf()) {
            codes.put(node.symbol, code);
            return;
        }
        buildCodeRecursive(node.left, code + "0");
        buildCodeRecursive(node.right, code + "1");
    }
}

package com.vid.compressor.entropy;

class HuffmanNode implements Comparable<HuffmanNode> {
    int symbol;
    int freq;
    HuffmanNode left, right;

    HuffmanNode(int symbol, int freq) {
        this.symbol = symbol;
        this.freq = freq;
    }

    HuffmanNode(HuffmanNode l, HuffmanNode r) {
        this.symbol = -1;
        this.freq = l.freq + r.freq;
        this.left = l;
        this.right = r;
    }

    boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public int compareTo(HuffmanNode o) {
        return Integer.compare(this.freq, o.freq);
    }
}

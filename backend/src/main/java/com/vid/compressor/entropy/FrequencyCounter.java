package com.vid.compressor.entropy;

public class FrequencyCounter {

    private final int[] frequencies = new int[256];

    public void add(int value) {
        frequencies[value & 0xFF]++;
    }

    public int[] getFrequencies() {
        return frequencies;
    }
}

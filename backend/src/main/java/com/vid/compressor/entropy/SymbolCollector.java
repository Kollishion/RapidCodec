package com.vid.compressor.entropy;

import java.util.HashMap;
import java.util.Map;

public class SymbolCollector {

    private final Map<Integer, Integer> freq = new HashMap<>();

    public void add(int symbol) {
         if (symbol < 0 || symbol > 255) {
        throw new IllegalArgumentException(
            "Symbol out of range: " + symbol);
        }
        freq.merge(symbol, 1, Integer::sum);
    }

    public Map<Integer, Integer> getFrequencies() {
        return freq;
    }

    public int[] toFrequencyArray() {
        int max = freq.keySet().stream().max(Integer::compare).orElse(0);
        int[] arr = new int[max + 1];
        for (var e : freq.entrySet()) {
            arr[e.getKey()] = e.getValue();
        }
        return arr;
    }
}


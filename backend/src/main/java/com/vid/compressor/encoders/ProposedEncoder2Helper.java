package com.vid.compressor.encoders;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vid.compressor.BitstreamWriter;
import com.vid.compressor.QuadtreeUtils;
import com.vid.compressor.entropy.HuffmanBitWriter;
import com.vid.compressor.entropy.HuffmanHeaderWriter;
import com.vid.compressor.entropy.HuffmanTable;
import com.vid.compressor.entropy.SymbolCollector;

public class ProposedEncoder2Helper {

    private HuffmanTable table;
    private boolean headerWritten = false;

    public void encodeBatch(List<int[][]> batch, BitstreamWriter rawWriter)
            throws IOException {

        if (batch == null || batch.isEmpty()) return;

        SymbolCollector collector = new SymbolCollector();
        Map<QuadtreeUtils.Key, Boolean> cache = new HashMap<>();

        /* ===== PASS 1: COLLECT ===== */
        for (int[][] frame : batch) {
            QuadtreeUtils.collectProposed(
                    0, 0,
                    QuadtreeUtils.CTU_SIZE,
                    frame,
                    cache,
                    collector
            );
        }

        if (!headerWritten) {
            table = HuffmanTable.build(collector.toFrequencyArray());
            HuffmanHeaderWriter.writeHeader(rawWriter, table);
            headerWritten = true;
        }

        HuffmanBitWriter hbw =
                new HuffmanBitWriter(rawWriter, table);

        /* ===== PASS 2: ENCODE ===== */
        for (int[][] frame : batch) {
            QuadtreeUtils.encodeProposed(
                    0, 0,
                    QuadtreeUtils.CTU_SIZE,
                    frame,
                    cache,
                    hbw
            );
        }

        hbw.flush();
        cache.clear();
    }
}

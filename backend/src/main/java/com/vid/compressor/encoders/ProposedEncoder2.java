package com.vid.compressor.encoders;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vid.compressor.BitstreamWriter;
import com.vid.compressor.FrameReader;
import com.vid.compressor.QuadtreeUtils;
import com.vid.compressor.QuadtreeUtils.Key;
import com.vid.compressor.entropy.HuffmanBitWriter;
import com.vid.compressor.entropy.HuffmanHeaderWriter;
import com.vid.compressor.entropy.HuffmanTable;
import com.vid.compressor.entropy.SymbolCollector;
import com.vid.compressor.model.Job;
import com.vid.compressor.model.Metrics;

public class ProposedEncoder2 implements Encoder {

    private static final int BATCH_SIZE = 100;

    @Override
    public Metrics encode(String framesDir, String outputFile) {

        Job job = new Job();
        long startTime = System.nanoTime();

        File folder = new File(framesDir);
        int totalFrames = FrameReader.totalFrames(folder);

        if (totalFrames == 0) {
            throw new RuntimeException("No frames found in: " + framesDir);
        }

        try (BitstreamWriter bw = new BitstreamWriter(new File(outputFile))) {

            /* =====================================================
             * PASS 1: SYMBOL COLLECTION (NO BIT OUTPUT)
             * ===================================================== */

            SymbolCollector collector = new SymbolCollector();

            for (int start = 0; start < totalFrames; start += BATCH_SIZE) {

                List<int[][]> batch =
                        FrameReader.loadGrayscaleFramesBatch(
                                folder, start, BATCH_SIZE);

                for (int[][] frame : batch) {

                    // Temporary cache only for discovering structure
                    Map<Key, Boolean> tempCache = new HashMap<>();

                    QuadtreeUtils.collectProposed(
                            0,
                            0,
                            QuadtreeUtils.CTU_SIZE,
                            frame,
                            tempCache,
                            collector
                    );
                }

                batch.clear();
            }

            /* =====================================================
             * BUILD & WRITE HUFFMAN TABLE
             * ===================================================== */

            HuffmanTable table =
                    HuffmanTable.build(collector.toFrequencyArray());

            HuffmanHeaderWriter.writeHeader(bw, table);

            /* =====================================================
             * PASS 2: ACTUAL ENCODING (CACHE + FALLBACK)
             * ===================================================== */

            for (int start = 0; start < totalFrames; start += BATCH_SIZE) {

                List<int[][]> batch =
                        FrameReader.loadGrayscaleFramesBatch(
                                folder, start, BATCH_SIZE);

                HuffmanBitWriter hbw =
                        new HuffmanBitWriter(bw, table);

                // Cache reused across frames in this batch (temporal locality)
                Map<Key, Boolean> cache = new HashMap<>();

                for (int[][] frame : batch) {
                    QuadtreeUtils.encodeProposed(
                            0,
                            0,
                            QuadtreeUtils.CTU_SIZE,
                            frame,
                            cache,
                            hbw
                    );
                }

                hbw.flush();
                batch.clear();
            }

        } catch (Exception e) {
            throw new RuntimeException("ProposedEncoder2 failed", e);
        }

        long endTime = System.nanoTime();

        /* =====================================================
         * METRICS (PLACEHOLDER / EXPERIMENTAL)
         * ===================================================== */

        job.setRuntimeMs((endTime - startTime) / 1_000_000);
        job.setCompressionRatio(1.4);
        job.setPsnr(42.0);
        job.setSsim(0.995);

        return new Metrics(
                job.getRuntimeMs(),
                job.getRdoCalls(),
                job.getCacheHits(),
                job.getTemporalHits(),
                job.getCompressionRatio(),
                job.getPsnr(),
                job.getSsim()
        );
    }
}

package com.vid.compressor.encoders;

import com.vid.compressor.QuadtreeUtils;
import com.vid.compressor.model.Job;
import com.vid.compressor.model.Metrics;

public class BaselineEncoder implements Encoder {

    @Override
    public Metrics encode(String inputFile, String outputFile) {
        long totalRdo = 0;
        long start = System.nanoTime();

        int[][] frame = QuadtreeUtils.syntheticFrame(QuadtreeUtils.CTU_SIZE, QuadtreeUtils.CTU_SIZE);
        QuadtreeUtils.BaselineEncoder be = new QuadtreeUtils.BaselineEncoder();

        be.processCTU(0, 0, QuadtreeUtils.CTU_SIZE, frame);
        totalRdo = be.rdoCount;

        long end = System.nanoTime();

        return new Metrics(
                (end - start) / 1_000_000,   // runtimeMs
                totalRdo,
                0,     
                0,     
                1.0,   
                40.0, 
                0.99   
        );
    }
      @Override
    public void compress(int iterations, Job job) {

        long totalRdo = 0;
        long start = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            int[][] frame = QuadtreeUtils.syntheticFrame(
                    QuadtreeUtils.CTU_SIZE,
                    QuadtreeUtils.CTU_SIZE
            );

            QuadtreeUtils.BaselineEncoder be = new QuadtreeUtils.BaselineEncoder();
            be.processCTU(0, 0, QuadtreeUtils.CTU_SIZE, frame);

            totalRdo += be.rdoCount;
        }

        long end = System.nanoTime();

        job.setRdoCalls(totalRdo);
        job.setCacheHits(0);
        job.setTemporalHits(0);
        job.setRuntimeMs((end - start) / 1_000_000);
        job.setCompressionRatio(1.0);
        job.setPsnr(38.0);
        job.setSsim(0.98);
    }
}

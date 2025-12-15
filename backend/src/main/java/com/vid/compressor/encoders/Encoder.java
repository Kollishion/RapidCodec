package com.vid.compressor.encoders;

import com.vid.compressor.model.Job;
import com.vid.compressor.model.Metrics;

public interface Encoder {
    Metrics encode(String inputFile, String outputFile);
    void compress(int iterations, Job job);
}

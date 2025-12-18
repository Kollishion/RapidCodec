package com.vid.compressor.encoders;

import com.vid.compressor.model.Metrics;

public interface Encoder {
    Metrics encode(String inputPath, String outputPath);
}

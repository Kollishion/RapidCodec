package com.vid.compressor.service;

import org.springframework.stereotype.Service;

import com.vid.compressor.encoders.BaselineEncoder;
import com.vid.compressor.encoders.ProposedEncoder;
import com.vid.compressor.encoders.ProposedEncoder2;
import com.vid.compressor.model.EncodeMode;
import com.vid.compressor.model.EncodeResponse;
import com.vid.compressor.model.Metrics;

@Service
public class EncoderService {

    private final BaselineEncoder baseline = new BaselineEncoder();
    private final ProposedEncoder proposed = new ProposedEncoder();
    private final ProposedEncoder2 proposed2 = new ProposedEncoder2();

    public EncodeResponse encode(String input, EncodeMode mode) {

        String outputFile = input.replace(".mp4", "_out.mp4");
        Metrics metrics = switch (mode) {
            case BASELINE -> baseline.encode(input, outputFile);
            case PROPOSED -> proposed.encode(input, outputFile);
            case PROPOSED2 -> proposed2.encode(input, outputFile);
        };

        return new EncodeResponse(outputFile, metrics);
    }
}

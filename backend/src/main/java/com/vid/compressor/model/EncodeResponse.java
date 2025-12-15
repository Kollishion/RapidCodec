package com.vid.compressor.model;

public class EncodeResponse {
    private String outputFile;
    private Metrics metrics;

    public EncodeResponse(String outputFile, Metrics metrics) {
        this.outputFile = outputFile;
        this.metrics = metrics;
    }

    public String getOutputFile() { return outputFile; }
    public Metrics getMetrics() { return metrics; }
}

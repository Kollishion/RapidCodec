package com.vid.compressor.model;

public class Metrics {

    private long runtimeMs;
    private long rdoCalls;
    private long cacheHits;
    private long temporalHits;
    private double compressionRatio;
    private double psnr;
    private double ssim;

    public Metrics(long runtimeMs, long rdoCalls, long cacheHits, long temporalHits,
                   double compressionRatio, double psnr, double ssim) {

        this.runtimeMs = runtimeMs;
        this.rdoCalls = rdoCalls;
        this.cacheHits = cacheHits;
        this.temporalHits = temporalHits;
        this.compressionRatio = compressionRatio;
        this.psnr = psnr;
        this.ssim = ssim;
    }

    public long getRuntimeMs() { return runtimeMs; }
    public long getRdoCalls() { return rdoCalls; }
    public long getCacheHits() { return cacheHits; }
    public long getTemporalHits() { return temporalHits; }
    public double getCompressionRatio() { return compressionRatio; }
    public double getPsnr() { return psnr; }
    public double getSsim() { return ssim; }

}

package com.vid.compressor.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    private UUID id;

    private String filename;

    private String mode;

    @Enumerated(EnumType.STRING)
    private Status status;

    private long rdoCalls;
    private long cacheHits;
    private long temporalHits;
    private long runtimeMs;
    private double compressionRatio;
    private double psnr; // placeholder metric
    private double ssim; // placeholder metric

    private Instant createdAt;
    private Instant finishedAt;

    public enum Status {
        PENDING, RUNNING, DONE, FAILED
    }

    public Job() {}

    public Job(UUID id, String filename, String mode) {
        this.id = id;
        this.filename = filename;
        this.mode = mode;
        this.status = Status.PENDING;
        this.createdAt = Instant.now();
    }

    // getters and setters (generated)
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getFilename() { return filename; }
    public void setFilename(String filename) { this.filename = filename; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public long getRdoCalls() { return rdoCalls; }
    public void setRdoCalls(long rdoCalls) { this.rdoCalls = rdoCalls; }
    public long getCacheHits() { return cacheHits; }
    public void setCacheHits(long cacheHits) { this.cacheHits = cacheHits; }
    public long getTemporalHits() { return temporalHits; }
    public void setTemporalHits(long temporalHits) { this.temporalHits = temporalHits; }
    public long getRuntimeMs() { return runtimeMs; }
    public void setRuntimeMs(long runtimeMs) { this.runtimeMs = runtimeMs; }
    public double getCompressionRatio() { return compressionRatio; }
    public void setCompressionRatio(double compressionRatio) { this.compressionRatio = compressionRatio; }
    public double getPsnr() { return psnr; }
    public void setPsnr(double psnr) { this.psnr = psnr; }
    public double getSsim() { return ssim; }
    public void setSsim(double ssim) { this.ssim = ssim; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
}

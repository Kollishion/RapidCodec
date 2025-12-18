package com.vid.compressor.service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.vid.compressor.encoders.BaselineEncoder;
import com.vid.compressor.encoders.Encoder;
import com.vid.compressor.encoders.ProposedEncoder;
import com.vid.compressor.encoders.ProposedEncoder2;
import com.vid.compressor.model.Job;
import com.vid.compressor.repository.JobRepository;

@Service
public class CompressionService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private Executor compressionExecutor;

    @Async("compressionExecutor")
    public void startCompression(UUID jobId, String mode, int iterations) {
        Optional<Job> jo = jobRepository.findById(jobId);
        if (jo.isEmpty()) return;
        Job job = jo.get();
        job.setStatus(Job.Status.RUNNING);
        jobRepository.save(job);
        try {
            Encoder encoder;
            switch (mode.toLowerCase()) {
                case "baseline": encoder = new BaselineEncoder(); break;
                case "proposed": encoder = new ProposedEncoder(); break;
                case "proposed2": encoder = new ProposedEncoder2(); break;
                default: encoder = new BaselineEncoder();
            }
            job.setStatus(Job.Status.DONE);
            job.setFinishedAt(java.time.Instant.now());
            jobRepository.save(job);
        } catch (Exception e) {
            job.setStatus(Job.Status.FAILED);
            jobRepository.save(job);
        }
    }

    public Job createJob(String filename, String mode) {
        Job job = new Job(UUID.randomUUID(), filename, mode);
        jobRepository.save(job);
        return job;
    }

    public Optional<Job> getJob(UUID id) {
        return jobRepository.findById(id);
    }
}

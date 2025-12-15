package com.vid.compressor.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vid.compressor.model.Job;
import com.vid.compressor.repository.JobRepository;
import com.vid.compressor.service.CompressionService;

@RestController
@RequestMapping("/api")
public class CompressorController {

    @Autowired
    private CompressionService compressionService;

    @Autowired
    private JobRepository jobRepository;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "mode", defaultValue = "proposed") String mode,
                                      @RequestParam(value = "frames", defaultValue = "20") int frames) throws Exception {
        String filename = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : "synthetic";
        Job job = compressionService.createJob(filename, mode);
        compressionService.startCompression(job.getId(), mode, frames);
        Map<String, Object> r = new HashMap<>();
        r.put("jobId", job.getId().toString());
        r.put("status", job.getStatus().toString());
        return r;
    }

    @GetMapping("/job/{id}")
    public ResponseEntity<Job> getJob(@PathVariable("id") UUID id) {
        return jobRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/jobs")
    public List<Job> listJobs() {
        return jobRepository.findAll();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<String> download(@PathVariable("id") UUID id) {
        return jobRepository.findById(id).map(j -> {
            String payload = String.format("jobId=%s\nmode=%s\nstatus=%s\nrdo=%d\ncacheHits=%d\ntemporalHits=%d\nruntimeMs=%d",
                    j.getId(), j.getMode(), j.getStatus(), j.getRdoCalls(), j.getCacheHits(), j.getTemporalHits(), j.getRuntimeMs());
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.TEXT_PLAIN);
            h.setContentDisposition(ContentDisposition.attachment().filename("job-" + j.getId() + ".txt").build());
            return new ResponseEntity<>(payload, h, HttpStatus.OK);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}

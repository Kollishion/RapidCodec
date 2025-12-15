package com.vid.compressor.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vid.compressor.model.Job;

public interface JobRepository extends JpaRepository<Job, UUID> {}

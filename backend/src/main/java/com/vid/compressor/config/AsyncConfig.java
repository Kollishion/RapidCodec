package com.vid.compressor.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    @Bean(name = "compressionExecutor")
    public Executor compressionExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        exec.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        exec.setQueueCapacity(50);
        exec.setThreadNamePrefix("compressor-");
        exec.initialize();
        return exec;
    }
}

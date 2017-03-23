package com.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class QueueService {

    private static final BlockingQueue<String> QUEUE = new LinkedBlockingQueue<>();

    @Autowired
    private QueueProcessor queueProcessor;

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public void add(String payload) throws Exception {
        QUEUE.add(payload);
        executorService.submit(queueProcessor);
    }

    BlockingQueue<String> getQueue() {
        return QUEUE;
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }

}

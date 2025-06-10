package com.example.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadFactory implements ThreadFactory {
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, "MyPool-worker-" + counter.incrementAndGet());
        System.out.println("[ThreadFactory] Creating new thread: " + thread.getName());
        return thread;
    }
}

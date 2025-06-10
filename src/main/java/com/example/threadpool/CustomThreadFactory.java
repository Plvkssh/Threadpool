package com.example.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomThreadFactory implements ThreadFactory {
    private static final Logger logger = Logger.getLogger(CustomThreadFactory.class.getName());
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String poolName;

    public CustomThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, poolName + "-worker-" + counter.incrementAndGet());
        thread.setUncaughtExceptionHandler((t, e) -> 
            logger.log(Level.SEVERE, "Thread " + t.getName() + " failed", e)
        );
        logger.log(Level.INFO, "Created new thread: " + thread.getName());
        return thread;
    }
}

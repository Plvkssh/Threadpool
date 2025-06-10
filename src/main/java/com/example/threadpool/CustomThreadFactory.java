package com.example.threadpool;

import java.util.concurrent.*;

public class Worker implements Runnable {
    private final BlockingQueue<Runnable> taskQueue;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private volatile boolean running = true;
    private Runnable initialTask;

    public Worker(Runnable initialTask, BlockingQueue<Runnable> taskQueue, 
                 long keepAliveTime, TimeUnit timeUnit) {
        this.initialTask = initialTask;
        this.taskQueue = taskQueue;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
    }

    public boolean isIdle() {
        return initialTask == null;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try {
            if (initialTask != null) {
                initialTask.run();
                initialTask = null;
            }

            while (running) {
                Runnable task = taskQueue.poll(keepAliveTime, timeUnit);
                if (task != null) {
                    task.run();
                } else if (taskQueue.isEmpty()) {
                    running = false;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

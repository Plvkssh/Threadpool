package com.example.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker implements Runnable {
    private static final Logger logger = Logger.getLogger(Worker.class.getName());
    private final BlockingQueue<Runnable> taskQueue;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private volatile boolean running = true;
    private Runnable initialTask;
    private final Thread currentThread;
    private final AtomicInteger completedTasks = new AtomicInteger(0);

    public Worker(Runnable initialTask, BlockingQueue<Runnable> taskQueue,
                 long keepAliveTime, TimeUnit timeUnit) {
        this.initialTask = initialTask;
        this.taskQueue = taskQueue;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.currentThread = Thread.currentThread();
    }

    public boolean isIdle() {
        return initialTask == null;
    }

    public void stopGracefully() {
        running = false;
    }

    public void stopNow() {
        running = false;
        currentThread.interrupt();
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
                    completedTasks.incrementAndGet();
                } else if (taskQueue.isEmpty()) {
                    running = false;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

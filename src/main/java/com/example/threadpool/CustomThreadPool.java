package com.example.threadpool;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomThreadPool implements CustomExecutor {
    private static final Logger logger = Logger.getLogger(CustomThreadPool.class.getName());
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;
    private final List<Worker> workers;
    private final TaskQueue taskQueue;
    private final CustomThreadFactory threadFactory;
    private final RejectedExecutionHandler rejectionHandler;
    private volatile boolean isShutdown = false;

    public CustomThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime,
                           TimeUnit timeUnit, int queueSize, int minSpareThreads) {
        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = minSpareThreads;
        this.workers = new ArrayList<>();
        this.taskQueue = new TaskQueue(queueSize);
        this.threadFactory = new CustomThreadFactory("CustomPool");
        this.rejectionHandler = new CustomRejectedHandler();
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            rejectionHandler.rejectedExecution(command, this);
            return;
        }

        if (workers.size() < corePoolSize || getFreeThreads() < minSpareThreads) {
            addWorker(command);
            return;
        }

        if (!taskQueue.offer(command)) {
            if (workers.size() < maxPoolSize) {
                addWorker(command);
            } else {
                rejectionHandler.rejectedExecution(command, this);
            }
        }
    }

    private synchronized void addWorker(Runnable firstTask) {
        Worker worker = new Worker(firstTask, taskQueue, keepAliveTime, timeUnit);
        Thread thread = threadFactory.newThread(worker);
        workers.add(worker);
        thread.start();
    }

    private synchronized int getFreeThreads() {
        return (int) workers.stream().filter(Worker::isIdle).count();
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> future = new FutureTask<>(callable);
        execute(future);
        return future;
    }

    @Override
    public synchronized void shutdown() {
        isShutdown = true;
        workers.forEach(Worker::stopGracefully);
    }

    @Override
    public synchronized void shutdownNow() {
        shutdown();
        taskQueue.clear();
        workers.forEach(Worker::stopNow);
    }
}

package com.example.threadpool;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CustomThreadPool implements CustomExecutor {
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;
    private final List<Worker> workers;
    private final BlockingQueue<Runnable> taskQueue;
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
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        this.threadFactory = new CustomThreadFactory();
        this.rejectionHandler = new CustomRejectedHandler();
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) {
            throw new RejectedExecutionException("ThreadPool is shutting down!");
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

    private void addWorker(Runnable firstTask) {
        Worker worker = new Worker(firstTask, taskQueue, keepAliveTime, timeUnit);
        Thread thread = threadFactory.newThread(worker);
        workers.add(worker);
        thread.start();
    }

    private int getFreeThreads() {
        int count = 0;
        for (Worker worker : workers) {
            if (worker.isIdle()) count++;
        }
        return count;
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> future = new FutureTask<>(callable);
        execute(future);
        return future;
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        for (Worker worker : workers) {
            worker.stop();
        }
    }

    @Override
    public void shutdownNow() {
        shutdown();
        taskQueue.clear();
    }
}

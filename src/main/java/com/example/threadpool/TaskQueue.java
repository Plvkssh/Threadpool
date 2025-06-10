package com.example.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaskQueue {
    private static final Logger logger = Logger.getLogger(TaskQueue.class.getName());
    private final BlockingQueue<Runnable> queue;
    private final int maxSize;

    public TaskQueue(int maxSize) {
        this.maxSize = maxSize;
        this.queue = new LinkedBlockingQueue<>(maxSize);
    }

    public boolean offer(Runnable task) {
        boolean result = queue.offer(task);
        if (!result) {
            logger.log(Level.WARNING, "Queue is full!");
        }
        return result;
    }

    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
        return queue.poll(timeout, unit);
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
    }
}

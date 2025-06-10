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
    private final AtomicInteger totalSubmitted = new AtomicInteger(0);
    private final AtomicInteger totalProcessed = new AtomicInteger(0);

    /**
     * Создает очередь с указанным максимальным размером.
     * 
     * @param maxSize максимальное количество задач в очереди
     * @throws IllegalArgumentException если maxSize <= 0
     */
    public TaskQueue(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Queue size must be positive");
        }
        this.maxSize = maxSize;
        this.queue = new LinkedBlockingQueue<>(maxSize);
        logger.log(Level.CONFIG, "TaskQueue initialized with capacity: {0}", maxSize);
    }

    /**
     * Добавляет задачу в очередь (без ожидания).
     * 
     * @param task задача для выполнения
     * @return true если задача добавлена, false если очередь полна
     * @throws NullPointerException если task == null
     */
    public boolean offer(Runnable task) {
        if (task == null) throw new NullPointerException("Task cannot be null");
        
        boolean result = queue.offer(task);
        if (result) {
            totalSubmitted.incrementAndGet();
            logger.log(Level.FINEST, "[Queue] Task offered: {0}", task);
        } else {
            logger.log(Level.FINE, "[Queue] Task rejected (queue full): {0}", task);
        }
        return result;
    }

  
    public boolean offer(Runnable task, long timeout, TimeUnit unit) 
            throws InterruptedException {
        if (task == null) throw new NullPointerException();
        
        boolean result = queue.offer(task, timeout, unit);
        if (result) {
            totalSubmitted.incrementAndGet();
            logger.log(Level.FINER, "[Queue] Task offered with wait: {0}", task);
        }
        return result;
    }

    /**
     * Извлекает задачу из очереди.
     * 
     * @return следующую задачу или null если очередь пуста
     */
    public Runnable poll() {
        Runnable task = queue.poll();
        if (task != null) {
            totalProcessed.incrementAndGet();
            logger.log(Level.FINEST, "[Queue] Task polled: {0}", task);
        }
        return task;
    }

    public Runnable poll(long timeout, TimeUnit unit) 
            throws InterruptedException {
        Runnable task = queue.poll(timeout, unit);
        if (task != null) {
            totalProcessed.incrementAndGet();
            logger.log(Level.FINER, "[Queue] Task polled with wait: {0}", task);
        }
        return task;
    }

    /**
     * Проверяет пустоту очереди.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Возвращает текущее количество задач в очереди.
     */
    public int size() {
        return queue.size();
    }

    /**
     * Очищает очередь.
     */
    public void clear() {
        int cleared = queue.size();
        queue.clear();
        logger.log(Level.INFO, "[Queue] Cleared {0} pending tasks", cleared);
    }

    /**
     * Возвращает общее количество принятых задач.
     */
    public int getTotalSubmitted() {
        return totalSubmitted.get();
    }

    /**
     * Возвращает общее количество обработанных задач.
     */
    public int getTotalProcessed() {
        return totalProcessed.get();
    }

    /**
     * Возвращает коэффициент загрузки очереди (0..1).
     */
    public double getLoadFactor() {
        return (double) size() / maxSize;
    }

    /**
     * Возвращает максимальный размер очереди.
     */
    public int getMaxSize() {
        return maxSize;
    }
}

package com.example.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
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
        
        logger.log(Level.FINE, "[Worker] Created new worker with initial task: {0}", 
                initialTask != null ? initialTask.getClass().getSimpleName() : "none");
    }

    /**
     * Проверяет, свободен ли воркер.
     * 
     * @return true если не выполняет задачу
     */
    public boolean isIdle() {
        return initialTask == null;
    }

    /**
     * Мягкое завершение работы воркера.
     * Завершится после выполнения текущей задачи.
     */
    public void stopGracefully() {
        running = false;
        logger.log(Level.FINE, "[Worker] Graceful stop requested");
    }

    /**
     * Немедленное завершение с прерыванием.
     */
    public void stopNow() {
        running = false;
        currentThread.interrupt();
        logger.log(Level.INFO, "[Worker] Immediate stop with interrupt");
    }

    /**
     * @return количество завершенных задач
     */
    public int getCompletedTasksCount() {
        return completedTasks.get();
    }

    @Override
    public void run() {
        try {
            // 1. Выполнить начальную задачу, если есть
            executeInitialTask();

            // 2. Основной цикл обработки задач
            while (running && !Thread.currentThread().isInterrupted()) {
                processNextTask();
            }
        } catch (InterruptedException e) {
            logger.log(Level.FINE, "[Worker] Worker interrupted during wait");
            Thread.currentThread().interrupt();
        } finally {
            cleanUp();
        }
    }

    private void executeInitialTask() {
        if (initialTask != null) {
            try {
                logger.log(Level.FINE, "[Worker] Executing initial task: {0}", initialTask);
                initialTask.run();
                completedTasks.incrementAndGet();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "[Worker] Initial task failed: " + e.getMessage(), e);
            } finally {
                initialTask = null;
            }
        }
    }

    private void processNextTask() throws InterruptedException {
        Runnable task = taskQueue.poll(keepAliveTime, timeUnit);
        if (task != null) {
            executeTask(task);
        } else if (shouldTerminate()) {
            running = false;
            logger.log(Level.FINE, "[Worker] Idle timeout reached, terminating");
        }
    }

    private void executeTask(Runnable task) {
        try {
            logger.log(Level.FINER, "[Worker] Starting task execution: {0}", task);
            task.run();
            completedTasks.incrementAndGet();
            logger.log(Level.FINER, "[Worker] Task completed: {0}", task);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "[Worker] Task failed: " + e.getMessage(), e);
        }
    }

    private boolean shouldTerminate() {
        return taskQueue.isEmpty() && isIdle();
    }

    private void cleanUp() {
        logger.log(Level.INFO, "[Worker] Worker terminated. Tasks completed: {0}", completedTasks);
    }
}

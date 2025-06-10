package com.example.threadpool;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CustomThreadPool implements CustomExecutor {
    private static final Logger logger = Logger.getLogger(CustomThreadPool.class.getName());

    // Конфигурационные параметры
    private final int corePoolSize;
    private final int maxPoolSize;
    private final long keepAliveTime;
    private final TimeUnit timeUnit;
    private final int queueSize;
    private final int minSpareThreads;

    // Внутренние компоненты
    private final List<Worker> workers;
    private final BlockingQueue<Runnable> taskQueue;
    private final CustomThreadFactory threadFactory;
    private final RejectedExecutionHandler rejectionHandler;

    // Состояние
    private volatile boolean isShutdown = false;
    private final AtomicInteger submittedTasks = new AtomicInteger(0);


    public CustomThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime,
                          TimeUnit timeUnit, int queueSize, int minSpareThreads) {
        // Валидация параметров
        if (corePoolSize < 0 || maxPoolSize <= 0 || maxPoolSize < corePoolSize || keepAliveTime < 0) {
            throw new IllegalArgumentException("Invalid pool configuration");
        }

        this.corePoolSize = corePoolSize;
        this.maxPoolSize = maxPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.timeUnit = timeUnit;
        this.queueSize = queueSize;
        this.minSpareThreads = Math.min(minSpareThreads, corePoolSize); // Не может быть больше corePoolSize

        this.workers = new ArrayList<>(maxPoolSize);
        this.taskQueue = new LinkedBlockingQueue<>(queueSize);
        this.threadFactory = new CustomThreadFactory("CustomPool");
        this.rejectionHandler = new CustomRejectedHandler();

        logger.log(Level.INFO, "ThreadPool initialized: core={0}, max={1}, queue={2}, minSpare={3}",
                new Object[]{corePoolSize, maxPoolSize, queueSize, minSpareThreads});
    }

    /**
     * Выполняет задачу в пуле потоков.
     *
     * @param command Задача для выполнения
     * @throws RejectedExecutionException Если пул завершает работу
     */
    @Override
    public void execute(Runnable command) {
        if (command == null) throw new NullPointerException();
        if (isShutdown) {
            rejectionHandler.rejectedExecution(command, this);
            return;
        }

        submittedTasks.incrementAndGet();
        logger.log(Level.FINE, "[Pool] Task submitted: {0}", command);

        // 1. Создать новый поток, если не хватает базовых или резервных
        if (workers.size() < corePoolSize || getFreeThreads() < minSpareThreads) {
            addWorker(command);
            return;
        }

        // 2. Попытаться добавить в очередь
        if (taskQueue.offer(command)) {
            logger.log(Level.FINER, "[Pool] Task queued: {0}", command);
        } else {
            // 3. Если очередь полная - создать новый поток, если возможно
            if (workers.size() < maxPoolSize) {
                addWorker(command);
            } else {
                // 4. Применить политику отказа
                logger.log(Level.WARNING, "[Pool] Task rejected (queue full): {0}", command);
                rejectionHandler.rejectedExecution(command, this);
            }
        }
    }

    /**
     * Добавляет новый рабочий поток в пул.
     *
     * @param firstTask Первая задача для выполнения (может быть null)
     */
    private synchronized void addWorker(Runnable firstTask) {
        if (workers.size() >= maxPoolSize) return;

        Worker worker = new Worker(firstTask, taskQueue, keepAliveTime, timeUnit);
        Thread thread = threadFactory.newThread(worker);
        workers.add(worker);
        thread.start();

        logger.log(Level.FINE, "[Pool] Worker added: {0} (total: {1})",
                new Object[]{thread.getName(), workers.size()});
    }

    /**
     * @return Количество свободных потоков
     */
    private synchronized int getFreeThreads() {
        int count = 0;
        for (Worker worker : workers) {
            if (worker.isIdle()) count++;
        }
        return count;
    }

    /**
     * Отправляет задачу с возвратом Future.
     *
     * @param callable Задача с возвращаемым значением
     * @return Future для отслеживания результата
     */
    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        FutureTask<T> future = new FutureTask<>(callable);
        execute(future);
        return future;
    }

    /**
     * Плавное завершение работы пула.
     * Дожидается выполнения всех задач в очереди.
     */
    @Override
    public synchronized void shutdown() {
        if (isShutdown) return;
        isShutdown = true;

        logger.log(Level.INFO, "[Pool] Shutdown initiated (tasks: {0}/{1})",
                new Object[]{taskQueue.size(), submittedTasks.get()});

        for (Worker worker : workers) {
            worker.stopGracefully();
        }
    }

    /**
     * Немедленное завершение работы пула.
     * Прерывает все потоки и очищает очередь.
     */
    @Override
    public synchronized void shutdownNow() {
        shutdown();
        taskQueue.clear();

        for (Worker worker : workers) {
            worker.stopNow();
        }

        logger.log(Level.INFO

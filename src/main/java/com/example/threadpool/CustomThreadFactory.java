package com.example.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CustomThreadFactory implements ThreadFactory {
    private static final Logger logger = Logger.getLogger(CustomThreadFactory.class.getName());
    private final AtomicInteger counter = new AtomicInteger(0);
    private final String poolName;

    /**
     * Создает фабрику для пула с указанным именем.
     * @param poolName Имя пула (для логирования и префикса потоков).
     */
    public CustomThreadFactory(String poolName) {
        this.poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable r) {
        // Формируем уникальное имя потока
        String threadName = poolName + "-worker-" + counter.incrementAndGet();
        Thread thread = new Thread(r, threadName);

        // Настраиваем обработчик неожиданных исключений для логирования завершения
        thread.setUncaughtExceptionHandler((t, e) -> 
            logger.log(Level.SEVERE, String.format("[Worker] Thread %s terminated due to uncaught exception: %s", t.getName(), e))
        );

        // Логируем создание потока
        logger.log(Level.INFO, "[ThreadFactory] Creating new thread: {0}", threadName);

        return thread;
    }

    /**
     * @return Общее количество созданных потоков.
     */
    public int getThreadsCreated() {
        return counter.get();
    }
}

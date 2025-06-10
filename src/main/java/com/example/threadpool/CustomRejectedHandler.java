package com.example.threadpool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;


public class CustomRejectedHandler implements RejectedExecutionHandler {
    private static final Logger logger = Logger.getLogger(CustomRejectedHandler.class.getName());

    /**
     * Обрабатывает отклоненную задачу.
     * 
     * @param r         Отклоненная задача (Runnable).
     * @param executor  Пул потоков, в котором произошел отказ.
     */
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // Формируем описание задачи для логов
        String taskInfo = (r != null) ? r.toString() : "null";
        
        // Логируем событие с уровнем WARNING
        logger.log(Level.WARNING, "[Rejected] Task \"{0}\" was rejected due to pool overload!", taskInfo);

        // Альтернативная стратегия: выполняем задачу в текущем потоке, если пул активен
        if (!executor.isShutdown()) {
            logger.log(Level.INFO, "[Fallback] Executing task \"{0}\" in caller thread.", taskInfo);
            r.run();
        } else {
            logger.log(Level.SEVERE, "[Rejected] Pool is shutting down. Task \"{0}\" discarded.", taskInfo);
        }
    }
}

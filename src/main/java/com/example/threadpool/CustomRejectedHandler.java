package com.example.threadpool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomRejectedHandler implements RejectedExecutionHandler {
    private static final Logger logger = Logger.getLogger(CustomRejectedHandler.class.getName());

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        logger.log(Level.WARNING, "Task rejected: " + r);
        if (!executor.isShutdown()) {
            r.run();
        }
    }
}

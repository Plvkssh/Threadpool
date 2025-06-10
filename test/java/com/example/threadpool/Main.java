package com.example.threadpool;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        CustomThreadPool pool = new CustomThreadPool(
            2,  // corePoolSize
            4,  // maxPoolSize
            5,  // keepAliveTime
            TimeUnit.SECONDS,
            5,  // queueSize
            1   // minSpareThreads
        );

        // Тестовые задачи
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            pool.execute(() -> {
                System.out.println("[Task] Started task #" + taskId + " in " + Thread.currentThread().getName());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("[Task] Finished task #" + taskId);
            });
        }

        Thread.sleep(10000);
        pool.shutdown();
    }
}

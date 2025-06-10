package com.example.threadpool;

public class Main {
    public static void main(String[] args) {
        CustomThreadPool pool = new CustomThreadPool(
            2, 4, 5, TimeUnit.SECONDS, 10, 1
        );

        for (int i = 0; i < 20; i++) {
            int taskId = i;
            pool.execute(() -> {
                System.out.println("Task " + taskId + " started");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        pool.shutdown();
    }
}

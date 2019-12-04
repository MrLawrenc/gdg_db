package application.utils;

import java.util.concurrent.*;

/**
 * 线程工具
 *
 * @author mrliu
 */
public class ThreadUtil {
    public static final ThreadPoolExecutor BLOCK_QUEUE_EXECUTOR = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() + 1, 20, 5,
            TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000),
            Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    static {
        BLOCK_QUEUE_EXECUTOR.prestartAllCoreThreads();
    }

    /**
     * 关闭线程池
     */
    static void closePoolExecutor(long timeout, TimeUnit unit) {
        try {
            BLOCK_QUEUE_EXECUTOR.shutdown();
            if (!BLOCK_QUEUE_EXECUTOR.awaitTermination(timeout, unit)) {
                System.out.println("线程池已关闭..........");
                BLOCK_QUEUE_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            // awaitTermination方法被中断的时候也中止线程池中全部的线程的执行。
            System.out.println("awaitTermination interrupted: " + e);
            BLOCK_QUEUE_EXECUTOR.shutdownNow();
        }
    }
}

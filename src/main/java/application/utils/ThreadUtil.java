package application.utils;

import java.util.concurrent.*;

/**
 * 线程工具
 *
 * @author mrliu
 */
public class ThreadUtil {
    public static final ThreadPoolExecutor BLOCK_QUEUE_EXECUTOR = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors() + 1, Runtime.getRuntime().availableProcessors() + 1,
            2, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000),
            Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    static {
        BLOCK_QUEUE_EXECUTOR.prestartAllCoreThreads();
    }

    static void closePoolExecutor(long timeout, TimeUnit unit) {
        try {
            // 向学生传达“问题解答完毕后请举手示意！”
            BLOCK_QUEUE_EXECUTOR.shutdown();

            // 向学生传达“XX分之内解答不完的问题全部带回去作为课后作业！”后老师等待学生答题
            // (所有的任务都结束的时候，返回TRUE)
            if (!BLOCK_QUEUE_EXECUTOR.awaitTermination(timeout, unit)) {
                // 超时的时候向线程池中所有的线程发出中断(interrupted)。
                BLOCK_QUEUE_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            // awaitTermination方法被中断的时候也中止线程池中全部的线程的执行。
            System.out.println("awaitTermination interrupted: " + e);
            BLOCK_QUEUE_EXECUTOR.shutdownNow();
        }
    }
}

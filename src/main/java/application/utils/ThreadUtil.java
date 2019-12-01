package application.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程工具
 * 
 * @author mrliu
 *
 */
public class ThreadUtil {
	public static final ThreadPoolExecutor BLOCK_QUEUE_EXECUTOR = new ThreadPoolExecutor(
			Runtime.getRuntime().availableProcessors() + 1, 60, 2, TimeUnit.MINUTES, new ArrayBlockingQueue<>(1000),
			Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
	
	static {
		BLOCK_QUEUE_EXECUTOR.prestartAllCoreThreads();
	}
}

package application.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Log {
	private String suffix = "\n";
	private OutputStreamWriter outStream = null;
	private FileOutputStream fos = null;
	public static Log log;
	public static final ThreadPoolExecutor BLOCK_QUEUE_EXECUTOR = new ThreadPoolExecutor(1, 1, 2,
			TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000), Executors.defaultThreadFactory(),
			new ThreadPoolExecutor.AbortPolicy());
	static {
		log = new Log();
	}
	private Log() {
		super();
		getOutputStream();
	}
	/**
	 * @Description 记录详细日志到文件
	 * @param type 0-->info 1 -->detail -1-->error
	 * @param info 日志信息
	 * @author Liu Mingyao
	 */
	public void writeLog(int type, String logStr) {
		System.out.println("logStr--->" + logStr);
		BLOCK_QUEUE_EXECUTOR.execute(() -> {
			String result = LocalDateTime.now().toString();
			if (type == 0) {
				result += "  info  " + logStr;
			} else if (type > 0) {
				result += "  detail   " + logStr;
			} else {
				result += "  error  " + logStr;
			}
			try {
				outStream.write(result + suffix);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private static OutputStreamWriter voidm() {
		String saveFile = "test.json";
		File file = new File(saveFile);
		FileOutputStream fos = null;
		OutputStreamWriter osw = null;

		try {
			if (!file.exists()) {
				boolean hasFile = file.createNewFile();
				if (hasFile) {
					System.out.println("file not exists, create new file");
				}
				fos = new FileOutputStream(file);
			} else {
				System.out.println("file exists");
				fos = new FileOutputStream(file, true);
			}

			osw = new OutputStreamWriter(fos, "utf-8");
			osw.write("测试内容"); // 写入内容
			osw.write("\r\n"); // 换行
			return osw;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) throws Exception {
		OutputStreamWriter voidm = voidm();
		voidm.write("wwwwwwwwwww");
		voidm.write("wwwwwwwwwww");
		log.writeLog(0, "sssssssssssssssssssss");
		log.writeLog(0, "sssssssssssssssssssss");
		log.writeLog(0, "sssssssssssssssssssss");
		log.writeLog(0, "sssssssssssssssssssss");
		Thread.sleep(10000);
		log.close();
	}
	/**
	 * 获取记录日志的文件流对象
	 * 
	 * @return OutputStreamWriter obj
	 * @author Liu Mingyao
	 */
	public OutputStreamWriter getOutputStream() {
		if (outStream != null) {
			return outStream;
		}

		File logFile = new File("log.gtdq");
		try {
			if (!logFile.exists()) {
				boolean hasFile = logFile.createNewFile();
				if (hasFile) {
					System.out.println("file not exists, create new file");
				}
				fos = new FileOutputStream(logFile);
			} else {
				System.out.println("file exists");
				fos = new FileOutputStream(logFile, true);
			}

			outStream = new OutputStreamWriter(fos, "utf-8");
			return outStream;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	// 关闭流
	public void close() {
		try {
			BLOCK_QUEUE_EXECUTOR.shutdown();
			System.out.println("====shutdown=====");
			while (!BLOCK_QUEUE_EXECUTOR.isTerminated()) {
				System.out.println("仍有线程在执行，稍后再关闭log文件流!");
				TimeUnit.MILLISECONDS.sleep(500);
			}
			System.out.println("关闭log文件流!");
			if (outStream != null) {
				outStream.close();
			}
			if (fos != null) {
				fos.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

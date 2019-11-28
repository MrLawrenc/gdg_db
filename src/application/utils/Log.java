package application.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.util.List;

public class Log {
	private String suffix = "\n";
	private OutputStreamWriter outStream = null;
	private FileOutputStream fos = null;
	public static Log log;
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
		try {
			ThreadUtil.BLOCK_QUEUE_EXECUTOR.execute(() -> {
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
					outStream.flush();
				} catch (IOException e) {
					System.out.println("输出流已关闭，以下信息不做记录:" + result);
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
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
					System.out.println("recored.gtdq file not exists, create new file");
				}
				fos = new FileOutputStream(logFile);
			} else {
				System.out.println("log.gtdq file exists");
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
			// 线程执行完毕再关闭流
			List<Runnable> shutdownNow = ThreadUtil.BLOCK_QUEUE_EXECUTOR.shutdownNow();
			shutdownNow.forEach(task -> {
				task.run();
				System.out.println("  " + shutdownNow.size());
			});

			System.out.println("关闭log文件流!");
			if (outStream != null) {
				outStream.flush();
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

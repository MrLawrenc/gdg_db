package application.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.concurrent.Task;

/**
 * 程序启动的时候异步清理log、recored、打包之后console输出的文件信息
 * 
 * @author mrliu
 *
 */
public class CleanTask extends Task<String> {

	@Override
	protected String call() throws Exception {
		File preLogCopyFile=null;
		long minLogCopyTime = 0;
		long minRecoredCopyTime = 0;
		List<File> recoredCopyFile = new ArrayList<File>();
		File file = new File(new File("").getAbsolutePath());
		if (file.isDirectory()) {
			for (File currentFile : file.listFiles()) {
				if (currentFile.isFile()) {
					if (currentFile.getName().contains("_copyLog")) {
						if (preLogCopyFile==null) {
							String time = currentFile.getName().split("_copyLog")[0];
							minLogCopyTime = Long.valueOf(time);
							preLogCopyFile = currentFile;
						}else {
							//比较
						}
					}
					if (currentFile.getName().contains("_copyRecored")) {
						recoredCopyFile.add(currentFile);
					}
				}

			}
		}

		return null;
	}

}

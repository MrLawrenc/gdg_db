package application.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.concurrent.Task;

/**
 * 程序启动的时候异步清理log、recorded、打包之后console输出的文件信息
 *
 * @author mrliu
 */
public class CleanTask extends Task<String> {

    @Override
    protected String call() throws Exception {
        File preLogCopyFile = null;
        File preRecordedCopyFile = null;
        long minLogCopyTime = 0;
        long minRecodedCopyTime = 0;
        File file = new File(new File("").getAbsolutePath());
        if (!file.isDirectory()) {
            return "";
        }
        for (File currentFile : Objects.requireNonNull(file.listFiles())) {
            if (!currentFile.isFile()) {
                continue;
            }
            if (currentFile.getName().contains("_copyLog")) {
                String time = currentFile.getName().split("_copyLog")[0];
                if (preLogCopyFile == null) {
                    minLogCopyTime = Long.parseLong(time);
                    preLogCopyFile = currentFile;
                } else if (Long.parseLong(time) < minLogCopyTime) {
                    currentFile.delete();
                } else {
                    preLogCopyFile.delete();
                    minLogCopyTime = Long.parseLong(time);
                    preLogCopyFile = currentFile;
                }
                continue;
            }
            if (currentFile.getName().contains("_copyRecored")) {
                String time = currentFile.getName().split("_copyRecored")[0];
                if (preRecordedCopyFile == null) {
                    minRecodedCopyTime = Long.parseLong(time);
                    preRecordedCopyFile = currentFile;
                } else if (Long.parseLong(time) < minRecodedCopyTime) {
                    currentFile.delete();
                } else {
                    preRecordedCopyFile.delete();
                    minRecodedCopyTime = Long.parseLong(time);
                    preRecordedCopyFile = currentFile;
                }
            }
        }
        return "";
    }

}

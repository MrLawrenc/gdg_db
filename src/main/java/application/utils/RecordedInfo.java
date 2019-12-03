package application.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

/**
 * 保存程序初始化信息-->url、username、password; 防止意外中断，会保存程序运行过程中信息，以便在下次启动继续更新数据入库;
 * 对于已入库的数据做标记;
 *
 * @author mrliu
 */
public class RecordedInfo {
    private OutputStreamWriter outStream = null;
    private FileOutputStream fos = null;
    public static RecordedInfo recored;
    /**
     * 记录信息的文件
     */
    public String recoredFileName = "recored.gtdq";
    /**
     * 完成的临时文件
     */
    // private String recordedTemp = "recored_temp.gtdq";

    public static String filePathPre = "&file:&";
    public static String fileState0 = "->0";
    public static String fileState1 = "->1";
    public static String mysqlInfo = "&mysql:&";

    public static String time = "&time:&";
    public static String other = "&mysql&";

    static {
        recored = new RecordedInfo();
    }

    private RecordedInfo() {
        super();
        getOutputStream();
    }


    /**
     * 保存文件状态
     *
     * @param done         是否保存完毕
     * @param fileFullPath 文件全路径
     */
    public synchronized void recoredFileInfo(boolean done, String fileFullPath) {
        try {
            if (outStream != null) {
                if (done) {
                    updateFile2State1(false, fileFullPath);
                    return;
                }
                synchronized (outStream) {
                    outStream.write(filePathPre + fileFullPath + fileState0 + "\n");
                    outStream.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getRecoredFile() {
        File file = new File(recoredFileName);
        if (!file.exists()) {
            Log.log.writeLog(-1, recoredFileName + "文件不存在!");
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 更新状态为已完成
     *
     * @param fileFullPath 文件全路径
     */
    private  void updateFile2State1(boolean isDelete, String fileFullPath) {
        File file = getRecoredFile();
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            String line = null;

            String newInfo = filePathPre + fileFullPath + fileState1 + "\n";
            String oldInfo = fileFullPath + fileState0;
            long seek = 0;
            while ((line = accessFile.readLine()) != null) {
                String resultLine = new String(line.getBytes("ISO-8859-1"), "UTF-8").trim();
                if (resultLine.contains(oldInfo)) {
                    accessFile.seek(seek);
                    //判断是改变状态还是删除记录
                    if (isDelete) {
                        Log.log.writeLog(0, "文件已回滚，删除记录:" + fileFullPath);
                        for (byte aByte : newInfo.getBytes()) {

                        }
                        //accessFile.write();
                    } else {
                        Log.log.writeLog(0, "更新文件状态为已入库:" + fileFullPath);
                        accessFile.write(newInfo.getBytes());
                    }
                    break;
                }
                seek += (resultLine + "\n").getBytes().length;
            }
        } catch (Exception e) {
        }
    }

    /**
     * 保存mysql连接信息 关于RandomAccessFile，不管源文件什么编码，读出来的都是iso-8859-1编码，需要转换为utf-8；String
     * resultLine = new String(line.getBytes("ISO-8859-1"), "UTF-8").trim();
     * 当read之后，pos会改变，如果下次再读会继续增加不会读到之前的内容，可以使用seek改变来重复读之前的内容；
     * 需要写的时候一定注意seek的使用，比如想从0开始写就必须seek（0），否则会是上一次操作之后的pos位置
     */
    public void recoredConnInfo(String url, String username, String password) {
        File file = getRecoredFile();
        try (RandomAccessFile accessFile = new RandomAccessFile(file, "rw")) {
            String line = null;
            boolean hasMysqlInfo = false;
            String newInfo = mysqlInfo + url + " " + username + " " + password;
            if ((line = accessFile.readLine()) != null) {
                String resultLine = new String(line.getBytes("ISO-8859-1"), "UTF-8").trim();
                if (resultLine.contains(mysqlInfo)) {
                    Log.log.writeLog(0, "更新数据库信息到本地 url:" + url + "  username:" + username + "  pwd:" + password);
                    // 构造和原来长度相等
                    if (newInfo.length() < resultLine.length()) {
                        long num = resultLine.length() - newInfo.length();
                        System.out.println("比原来短" + num + "，增加空格补位");
                        for (int i = 0; i < num; i++) {
                            newInfo += " ";
                        }
                        newInfo += "\n";
                    }
                    accessFile.seek(0);
                    accessFile.write(newInfo.getBytes(), 0, newInfo.length());
                    hasMysqlInfo = true;
                }
            }
            if (!hasMysqlInfo) {
                Log.log.writeLog(0, "保存数据库信息到本地 url:" + url + "  username:" + username + "  pwd:" + password);
                accessFile.seek(0);
                byte[] data = new byte[(int) accessFile.length()];
                accessFile.read(data, 0, data.length);
                accessFile.seek(0);
                newInfo += "\n";
                accessFile.write(newInfo.getBytes(), 0, newInfo.length());
                if (data.length > 1) {
                    accessFile.seek(newInfo.length());
                    accessFile.write(data, 0, data.length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     *
     */
    public Map<String, String> getFileInfo(String fileLine) {
        Map<String, String> fileMap = new HashMap<String, String>();
        return fileMap;
    }

    /**
     * 按行读文件方法 fileName 如 recoredTemp.gtdq
     */
    public void readFile(String fileName, String filePath) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("记录文件不存在");
            file.createNewFile();
        }
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        // 简写如下
        // BufferedReader br = new BufferedReader(new InputStreamReader(
        // new FileInputStream("E:/phsftp/evdokey/evdokey_201103221556.txt"), "UTF-8"));
        String line = "";
        while ((line = br.readLine()) != null) {
            System.out.println("line:" + line + "  _>" + br.readLine());
            break;
            // String replace = line.replace(filePath + fileState0, filePath + fileState1);
            // // 添加换行符
            // // tempStream.append(System.getProperty("line.separator"))
            // System.out.println("替换之后的字符串行：" + replace);
        }
        br.close();
        isr.close();
        fis.close();
    }

    @SuppressWarnings("resource")
    public static void main(String[] args) throws Exception {
        new RecordedInfo().recoredFileInfo(false, "E:\\工电供资料\\document\\6.客户资料");
        new RecordedInfo().recoredConnInfo(
                "jdbc:mysql://47.96.sss158.221:3306/study?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTisssssssssmezone=UTC",
                "user1s", "admin1s");
        new RecordedInfo().recoredFileInfo(true, "E:\\工电供资料\\document\\6.客户资料");

        // RandomAccessFile raf = new RandomAccessFile(new File("log.gtdq"), "rw");
        // System.out.println(raf.length());
        // raf.seek(raf.length());
        // raf.write("我是爱sas美丽\n".getBytes("utf-8"));
        //
        // raf.seek(0);
        // raf.write(" ".getBytes(), 0, 1);
        // RandomAccessFile 读写文件时，不管文件中保存的数据编码格式是什么 使用 RandomAccessFile对象方法的 readLine()
        // 都会将编码格式转换成 ISO-8859-1 所以 输出显示是还要在进行一次转码
        // System.out.println(new String(raf.readLine().getBytes("ISO-8859-1"),
        // "UTF-8"));
        // System.out.println(new String(raf.readLine().getBytes("ISO-8859-1"),
        // "UTF-8"));
        // System.out.println("s||||".split("s")[1]);
        // RecordedInfo recoredInfo = new RecordedInfo();
        // recoredInfo.readFile("recored.gtdq",
        // "E:\\工电供资料\\document\\6.客户资料\\工务\\工务检测数据\\2019年第三季度综合检测车联检\\京原线\\京原线_原平_(410-237)\\京原线_原平_(410-237).iic");
    }

    private OutputStreamWriter getOutputStream() {
        if (outStream != null) {
            return outStream;
        }
        File logFile = new File(recoredFileName);
        // 大于300m就拷贝文件,并清空源文件
        if (Util.getMegaNum(logFile.length()) > 300) {
            Util.copyFileUsingFileChannels(logFile, new File(System.currentTimeMillis() + "_copyRecored.gtdq"));
            logFile.delete();
        }
        try {
            if (!logFile.exists()) {
                boolean hasFile = logFile.createNewFile();
                if (hasFile) {
                    System.out.println("recored.gtdq file not exists, create new file");
                }
                fos = new FileOutputStream(logFile);
            } else {
                System.out.println("recored.gtdq file exists");
                fos = new FileOutputStream(logFile, true);
            }

            outStream = new OutputStreamWriter(fos, "utf-8");
            return outStream;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭流
     */
    public void close() {
        try {
            synchronized (outStream) {
                System.out.println("关闭recored文件流!");
                if (outStream != null) {
                    outStream.flush();
                    outStream.close();
                }
                if (fos != null) {
                    fos.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

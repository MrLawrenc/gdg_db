package application.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import application.db.MySqlUtil;

/**
 * @author : LiuMing
 * @date : 2019/12/2 9:47
 * @description :   TODO
 */
public class Util {
    public static List<String> exceptionName = new ArrayList<>();
    public static String[] tables = new String[]{"tvalue", "tqi", "kms", "defects", "curves"};

    /**
     * 根据文件名获取其他信息 <br>
     * 吕临线_太原南_(35.950-3.848) <br>
     * 京包线上行_大同_(371-236.5)
     */
    public static String[] getInfo(File file) {
        String dbFullName = file.getName();
        Log log = Log.log;
        // 线路
        String lineName = "";
        // 行别
        String direction = "";
        // 工务段
        String powerSection = "";
        try {
            String[] split = dbFullName.split("_");
            if (split[0].contains("行")) {
                lineName = split[0].substring(0, split[0].length() - 2);
                direction = split[0].substring(split[0].length() - 2);
            } else {
                lineName = split[0];
                direction = "0";
            }
            powerSection = split[1].trim() + "工务段";
        } catch (Exception e) {
            log.writeLog(-1, "异常:" + dbFullName + ExceptionUtil.appendExceptionInfo(e));
            exceptionName.add(file.getAbsolutePath());
            System.out.println("文件名解析失败，将使用默认值(文件名异常)保存!");
            return new String[]{"文件名异常", "文件名异常", "文件名异常"};
        }
        return new String[]{lineName.trim(), direction.trim(), powerSection.trim()};
    }

    /**
     * 根据文件夹切分信息，存如rpt_gw_sumary表
     *
     * @param fileName 文件夹名字
     */
    public static String[] getAbstractInfo(String fileName) {
        String[] split = fileName.split("_");
        if (split.length != 4) {
            Log.log.writeLog(-1, "文件名" + fileName + "不符合规范!");
            return new String[]{"", "", "", ""};
        }
        // 年份
        String year = split[0];
        // 季度
        String quarter = split[1];
        // 检测设备名字
        String detectDevName = split[2];
        // 检测方式
        String detectWay = split[3];
        return new String[]{year, quarter, detectDevName, detectWay};
    }

    /**
     * 退出程序做的资源清理
     */
    public static void exit() {
        System.out.println("准备关闭线程池.........");
        Log.log.writeLog(0, "退出，即将关闭线程池、连接池、文件流等资源.......................");
        ThreadUtil.closePoolExecutor(5, TimeUnit.SECONDS);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MySqlUtil.close0();
        Log.log.close();

        RecordedInfo.recored.close();
        System.exit(0);
    }

    /**
     * 获取所有线路所在的目录
     */
    public static void searchFile(File parentFile, Set<File> result) {
        File[] files = parentFile.listFiles();
        if (files == null) {
            return;
        }
        for (File currentFile : files) {
            if (currentFile.isDirectory()) {
                if ((currentFile.getName().contains("上行") || currentFile.getName().contains("下行"))) {
                    result.add(currentFile.getParentFile());
                    return;
                } else {
                    searchFile(currentFile, result);
                }
            }

        }
    }

    /**
     * 精度保证
     */
    public static double round(double v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        String s = String.valueOf(v);
        // true:小数需要补位 否则是截取 规则四舍五入
        if (s.split("\\.")[1].length() < scale) {
            String formatStr = String.format("%." + scale + "f", v);
            v = Double.parseDouble(formatStr);
        }
        BigDecimal b = BigDecimal.valueOf(v);
        BigDecimal one = new BigDecimal("1");
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 根据文件长度计算文件大小，单位m
     *
     * @param fileLength
     * @return
     */
    public static long getMegaNum(long fileLength) {
        return fileLength / 1024 / 1024;
    }

    /**
     * NIO文件拷贝
     *
     * @param source 源
     * @param dest   目标
     * @throws IOException
     */
    public static void copyFileUsingFileChannels(File source, File dest) {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            try {
                inputChannel.close();
                outputChannel.close();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    /**
     * 根据date和time转换为dataTime类型
     *
     * @param date RunDate 2019/8/30 00:00:00.0000000
     * @param time RunTime 20:23:04
     * @return
     */
    public static String getDataTime(String date, String time) {
        if (date == null) {
            return "2019-11-27 14:10:03";
        } else {
            String resultRunData = date.replaceAll("/", "-").substring(0, 10);
            if (time == null) {
                return resultRunData + " 00:00:00";
            } else {
                return resultRunData + " " + time;
            }
        }
    }

    /**
     * 拼接基础的局信息
     */
    public static StringBuilder addBaseInfo(StringBuilder sql) {
        sql.append("\"太原铁路局\",");
        sql.append("\"TYJ$J04\",");
        return sql;
    }
}

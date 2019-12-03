package application.utils;

import application.controller.MyController.Cache;
import application.db.AccessUtil;
import application.db.MySqlUtil;
import application.table.Defects;
import application.table.Kms;
import application.table.TValueUtil;
import application.table.TqiUtil;
import javafx.concurrent.Task;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * 批量入库异步线程类
 *
 * @author mrliu
 */
public class MyTask extends Task<String> {
    /**
     * 数据入库模式选择，默认单个数据库入一次库
     */
    private boolean useBatch = false;

    private Cache cache;

    public MyTask(File parentFile, Cache cache) {
        this.parentFile = parentFile;
        this.cache = cache;
    }

    private File parentFile;
    /**
     * 每张表的表头和所有行记录的 key前缀
     */
    private String tableNamePre = "name:";
    private String tableDataPre = "data:";

    /**
     * 暴露给外部更新日志的方法
     */
    public void log(String message) {
        updateMessage(message);
    }

    /**
     * 这儿jfx有一个bug，当消息冲刷太快，会导致task的监听，即task.messageProperty().addListener收到的消息不完整，前面的消息会被冲刷掉，
     * 因此这儿临时解决，每个消息到达，睡眠10ms.加入线程池队列可以不阻塞updateMessage方法的调用方
     */
    @Override
    protected void updateMessage(String message) {
        try {
            ThreadUtil.BLOCK_QUEUE_EXECUTOR.execute(() -> {
                super.updateMessage(message);
                try {
                    Thread.sleep(2);
                } catch (Exception e) {

                }
            });
        } catch (Exception e) {
            // TODO Auto-generated catch block
        }
    }

    /**
     * 异步方法，类似于Run()
     */
    @Override
    protected String call() {
        List<File> allDbFiles = AccessUtil.accordRequireFiles(parentFile);
        if (allDbFiles.size() == 0) {
            return "0该文件夹下没数据！";
        }
        delDoneFile(allDbFiles);
        if (allDbFiles.size() == 0) {
            return "1文件夹下数据都已入过库！";
        }

        Log.log.writeLog(0, "db数量：" + allDbFiles.size());
        updateProgress(3, 100);
        if (useBatch) {
            batchDbAdd(allDbFiles);
        } else {
            double size = allDbFiles.size();
            double temp = Util.round(3, 5);
            double everyNum = Util.round(94.00000 / Util.round(size, 5), 5);
            for (File file : allDbFiles) {

                singleDbAdd(temp, everyNum, file);
                temp += everyNum;
                updateProgress(temp, 100);
            }
            System.out.println("总共插入数据:tqi->" + TqiUtil.sum + "  tvalue->" + TValueUtil.sum + "  defects->"
                    + Defects.sum + "  kms->" + Kms.sum);
        }
        updateProgress(97, 100);
        //执行更新操作
        executeSqlUpdate();
        updateProgress(100, 100);
        return "总共插入数据:tqi->" + TqiUtil.sum + "  tvalue->" + TValueUtil.sum + "  defects->" + Defects.sum + "  kms->"
                + Kms.sum + "\n";
    }

    /**
     * 批量access数据库处理更新
     */
    private void batchDbAdd(List<File> allDbFiles) {
        try {
            int limit = 2;
            int dbNum = allDbFiles.size();
            String aaString = "源数据库数量:" + dbNum;
            updateMessage(aaString);

            if (dbNum > limit) {
                int num = dbNum / limit;
                for (int i = 0; i < num; i++) {
                    updateMessage("处理 " + (i * limit) + " 到 " + (i + 1) * limit);
                    batchAdd0(allDbFiles.subList(i * limit, (i + 1) * limit));
                }
                if (num * limit != dbNum) {
                    updateMessage("处理 " + (num * limit) + " 到 " + dbNum);
                    batchAdd0(allDbFiles.subList(num * limit, dbNum));
                }
            }
            updateMessage("数据插入成功......................");
            executeSqlUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            Log.log.writeLog(-1, "数据插入失败！" + ExceptionUtil.appendExceptionInfo(e));
        }
    }

    /**
     * @param start 进度条开始位置
     * @param step  进度条在该方法内能使用的数量
     * @description 单个access数据库处理，主要是方便得知进度条，否则应使用批量插入，节约时间
     */
    private void singleDbAdd(double start, double step, File dbFile) {
        try {
            Map<String, List<List<String>>> allDataMap = new HashMap<>(1000);
            Log.log.writeLog(1, "正在获取数据库文件" + dbFile.getName() + "的所有数据");
            String dbPath = dbFile.getAbsolutePath();
            List<String> allTableName = AccessUtil.allTableName(dbPath);
            for (String tableName : allTableName) {
                String tableNameKey = tableNamePre + tableName;
                String tableDataKey = tableDataPre + tableName;
                List<List<String>> tableData = AccessUtil.getTableDataByTableName(dbPath, tableName);
                List<List<String>> metaList = new ArrayList<List<String>>(1);
                metaList.add(tableData.get(0));
                allDataMap.putIfAbsent(tableNameKey, metaList);
                // 删除表头信息
                tableData.remove(0);
                System.out.println(tableName + "  表数据  " + tableData.size());
                if (allDataMap.get(tableDataKey) == null) {
                    allDataMap.put(tableDataKey, tableData);
                } else {
                    allDataMap.get(tableDataKey).addAll(tableData);
                }
            }
            AccessUtil.closeConn(dbPath);

            addOtherInfo4Single(dbFile, allDataMap);
            updateProgress(start + step / 3.00000, 100);

            dealAllData(dbFile.getAbsolutePath(), allDataMap);
        } catch (Exception e) {
            Log.log.writeLog(-1, "0异常，程序即将关闭" + ExceptionUtil.appendExceptionInfo(e));
        }

    }

    /**
     * 对所有的db进行数据获取，并插入mysql
     *
     * @param allFiles 所有的db文件
     */
    private void batchAdd0(List<File> allFiles) {
        try {
            Map<String, List<List<String>>> allDataMap = new HashMap<>(1000);
            for (File dbFile : allFiles) {
                Log.log.writeLog(1, "正在获取数据库文件" + dbFile.getName() + "的所有数据");

                String dbPath = dbFile.getAbsolutePath();
                List<String> allTableName = AccessUtil.allTableName(dbPath);
                for (String tableName : allTableName) {
                    // logUtil.infoLog("tableName:" + tableName);
                    String tableNameKey = tableNamePre + tableName;
                    String tableDataKey = tableDataPre + tableName;
                    List<List<String>> tableData = AccessUtil.getTableDataByTableName(dbPath, tableName);
                    List<List<String>> metaList = new ArrayList<>(1);
                    metaList.add(tableData.get(0));
                    allDataMap.putIfAbsent(tableNameKey, metaList);
                    // 删除表头信息
                    tableData.remove(0);
                    if (allDataMap.get(tableDataKey) == null) {
                        allDataMap.put(tableDataKey, tableData);
                    } else {
                        allDataMap.get(tableDataKey).addAll(tableData);
                    }
                }
                AccessUtil.closeConn(dbPath);
            }
            addOtherInfo(allFiles, allDataMap);

            // dealAllData(allDataMap);

        } catch (Exception e) {
            Log.log.writeLog(-1, "异常，程序即将关闭" + ExceptionUtil.appendExceptionInfo(e));
        }

    }

    /**
     * 数据插入完毕之后执行更新脚本
     */
    private void executeSqlUpdate() {
        Log.log.writeLog(0, "开始执行sql更新脚本!");

        //再更新所有记录表
        Connection connection = MySqlUtil.getConnection();
        try {
            synchronized (connection) {
                if (!connection.isClosed()) {
                    updateSql0(connection);
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateMessage("&更新批次信息失败！");
        System.out.println("更新批次信息失败！");
    }

    /**
     * 更新几张表批次相关字段
     */
    private void updateSql0(Connection connection) {
        String[] abstractInfo = Util.getAbstractInfo(parentFile.getName());
        String detectName = "综合检测";
        if (!abstractInfo[2].contains(detectName)) {
            detectName = "单专业";
        }
        String id = UUID.randomUUID().toString();
        //先更新批次表
        String updateSql = "insert into rpt_gw_sumary(detect_batch_id,detect_batch_name,detect_device_code,detect_name) " +
                "values ( \"" + id + "\",\"" + abstractInfo[0] + abstractInfo[1] + "\",\"" + abstractInfo[2]
                + "\",\"" + detectName + "\");";
        try {
            Statement statement = connection.createStatement();
            int i = statement.executeUpdate(updateSql);
            connection.commit();
            statement.close();
            Log.log.writeLog(0, "批次主表更新完毕！");
            updateMessage("批次主表更新完毕！");
            if (i == 1) {
                //更新其他表
                String sql1 = "UPDATE rpt_gw_tqi SET abstract_id=\"" + id + "\" WHERE ISNULL(abstract_id);";
                String sql2 = "UPDATE rpt_gw_kmscore SET abstract_id=\"" + id + "\" WHERE ISNULL(abstract_id);";
                String sql3 = "UPDATE rpt_gw_tvalue SET abstract_id=\"" + id + "\" WHERE ISNULL(abstract_id);";
                int i1 = connection.createStatement().executeUpdate(sql1);
                int i2 = connection.createStatement().executeUpdate(sql2);
                int i3 = connection.createStatement().executeUpdate(sql3);
                connection.commit();
                updateMessage("tqi表更新数量:" + i1 + "\tkms表更新数量:" + i2 + "\ttvalue表更新数量：" + i3);
                Log.log.writeLog(0, "tqi表更新数量:" + i1 + "\tkms表更新数量:" + i2 + "\ttvalue表更新数量：" + i3);
            }

        } catch (SQLException e) {
            Log.log.writeLog(0, "批次相关信息更新异常.....................");
            e.printStackTrace();
        }
        Log.log.writeLog(0, "执行sql更新脚本成功!");
    }

    /**
     * 添加线路、行别等信息
     */
    private void addOtherInfo(List<File> allFiles, Map<String, List<List<String>>> allDataMap) {
        for (File file : allFiles) {
            String[] info = Util.getInfo(file);
            List<List<String>> tvalue = allDataMap.get(tableDataPre + Util.tables[0]);
            add0(tvalue, info);
            List<List<String>> tqi = allDataMap.get(tableDataPre + Util.tables[1]);
            add0(tqi, info);
            List<List<String>> kms = allDataMap.get(tableDataPre + Util.tables[2]);
            add0(kms, info);
            List<List<String>> defects = allDataMap.get(tableDataPre + Util.tables[3]);
            add0(defects, info);
        }

    }

    private void addOtherInfo4Single(File file, Map<String, List<List<String>>> allDataMap) {
        String[] info = Util.getInfo(file);
        List<List<String>> tvalue = allDataMap.get(tableDataPre + Util.tables[0]);
        add0(tvalue, info);
        List<List<String>> tqi = allDataMap.get(tableDataPre + Util.tables[1]);
        add0(tqi, info);
        List<List<String>> kms = allDataMap.get(tableDataPre + Util.tables[2]);
        add0(kms, info);
        List<List<String>> defects = allDataMap.get(tableDataPre + Util.tables[3]);
        add0(defects, info);

    }

    private void add0(List<List<String>> data, String[] info) {
        data.forEach(list -> {
            list.add(info[0]);
            list.add(info[1]);
            list.add(info[2]);
        });
    }

    /**
     * 保存所有数据到mysql
     */
    private void dealAllData(String dbPath, Map<String, List<List<String>>> allData) throws InterruptedException {

        RecordedInfo.recored.recoredFileInfo(false, dbPath + " tvalue");
        boolean tvalueSuccess = TValueUtil.save(allData.get(tableNamePre + Util.tables[0]),
                allData.get(tableDataPre + Util.tables[0]), this);
        if (tvalueSuccess) {
            RecordedInfo.recored.recoredFileInfo(true, dbPath + " tvalue");
        }
        allData.remove(tableDataPre + Util.tables[0]);

        RecordedInfo.recored.recoredFileInfo(false, dbPath + " tqi");
        boolean tqiSuccess = TqiUtil.save(allData.get(tableNamePre + Util.tables[1]),
                allData.get(tableDataPre + Util.tables[1]), this);
        if (tqiSuccess) {
            RecordedInfo.recored.recoredFileInfo(true, dbPath + " tqi");
        }
        allData.remove(tableDataPre + Util.tables[1]);

        RecordedInfo.recored.recoredFileInfo(false, dbPath + " kms");
        boolean kmsSuccess = Kms.save(allData.get(tableNamePre + Util.tables[2]),
                allData.get(tableDataPre + Util.tables[2]), this);
        allData.remove(tableDataPre + Util.tables[2]);
        if (kmsSuccess) {
            RecordedInfo.recored.recoredFileInfo(true, dbPath + " kms");
        }
        // alarm表
        RecordedInfo.recored.recoredFileInfo(false, dbPath + " defects");
        boolean defectsSuccess = Defects.save(allData.get(tableNamePre + Util.tables[3]),
                allData.get(tableDataPre + Util.tables[3]), this);
        if (defectsSuccess) {
            RecordedInfo.recored.recoredFileInfo(true, dbPath + " defects");
        }
        allData.remove(tableDataPre + Util.tables[3]);

    }

    /**
     * 剔除本次扫描到文件夹（删除之前入过库的重复db文件）
     */
    private void delDoneFile(List<File> resultFile) {
        for (String finishPath : cache.doneFilePathList) {
            resultFile.removeIf(value -> finishPath.contains(value.getAbsolutePath()));
        }
    }
}
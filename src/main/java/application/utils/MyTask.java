package application.utils;

import application.db.AccessUtil;
import application.db.MySqlUtil;
import application.table.*;
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


    public MyTask(File parentFile) {
        this.parentFile = parentFile;
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
        if (allDbFiles.size() == 0) {
            return "1文件夹下数据都已入过库！";
        }

        Log.log.writeLog(0, "db数量：" + allDbFiles.size());
        updateProgress(3, 100);
        if (useBatch) {
            System.out.println("已删除大批量入库方法..............");
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

            dealAllData(dbFile.getName(), allDataMap);
        } catch (Exception e) {
            e.printStackTrace();
            Log.log.writeLog(-1, "0异常，程序即将关闭" + ExceptionUtil.appendExceptionInfo(e));
        }

    }


    /**
     * 数据插入完毕之后执行更新脚本
     */
    private void executeSqlUpdate() {
        Log.log.writeLog(0, "开始执行sql更新脚本!");

        //再更新所有记录表
//        Connection connection = MySqlUtil.getConnection();
        Connection connection = null;
        try {
            connection = MySqlUtil.getConn0();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
        try {
            if (!connection.isClosed()) {
                updateSql0(connection);
                MySqlUtil.returnConn(connection);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        MySqlUtil.returnConn(connection);
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
                updateMessage("\n\ttqi表更新数量:" + i1 + "\n\tkms表更新数量:" + i2 + "\n\ttvalue表更新数量：" + i3);
                Log.log.writeLog(0, "tqi表更新数量:" + i1 + "\tkms表更新数量:" + i2 + "\ttvalue表更新数量：" + i3);
            }

        } catch (SQLException e) {
            Log.log.writeLog(0, "批次相关信息更新异常.....................");
            e.printStackTrace();
        }
        Log.log.writeLog(0, "执行sql更新脚本成功!");
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
    private void dealAllData(String dbFileName, Map<String, List<List<String>>> allData) {
        executeSql(allData, dbFileName, 0);
        executeSql(allData, dbFileName, 1);
        executeSql(allData, dbFileName, 2);
        executeSql(allData, dbFileName, 3);
    }

    /**
     * 分发执行sql实现，根据结果设置本次插入记录
     */
    private void executeSql(Map<String, List<List<String>>> allData, String dbFileName, int tableNameIndex) {
        DataStoreRecordPo startSave = new DataStoreRecordPo(parentFile.getName(), dbFileName, Util.tables[tableNameIndex]);
        DataStoreRecord.startSave(startSave);
        boolean success;
        switch (tableNameIndex) {
            case 0:
                success = TValueUtil.save(allData.get(tableDataPre + Util.tables[tableNameIndex]), this);
                break;
            case 1:
                success = TqiUtil.save(allData.get(tableDataPre + Util.tables[tableNameIndex]), this);
                break;
            case 2:
                success = Kms.save(allData.get(tableDataPre + Util.tables[tableNameIndex]), this);
                break;
            case 3:
                success = Defects.save(allData.get(tableDataPre + Util.tables[tableNameIndex]), this);
                break;
            default:
                System.out.println("=====没有相应实现=====");
                success = false;
        }
        if (success) {
            DataStoreRecord.saveSuccess(startSave.toSuccess());
        }
        allData.remove(tableDataPre + Util.tables[tableNameIndex]);
    }

}
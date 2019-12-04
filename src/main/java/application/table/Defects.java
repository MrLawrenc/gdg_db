package application.table;

import application.db.MySqlUtil;
import application.utils.ExceptionUtil;
import application.utils.Log;
import application.utils.MyTask;
import application.utils.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

public class Defects {
    public static long sum = 0;
    // alarm表需要的字段，
    // RunTime,bureau_code,bureau_name,power_section_code,power_section_name,profession,id,svalue12,
    // line_code,line_name,raised_time,km_mark,created_time,code_name,direction,
    // nvalue12,nvalue11, severity,
    // nvalue4,nvalue3,svalue2,nvalue2,nvalue1,
    // nvalue5,svalue1,code,nvalue6,svalue4,svalue3,
    /**
     * 部分数据库不需要的字段为“”，后面会直接跳过
     */
    private static String[] fields = new String[]{"id", "bureau_name", "bureau_code", "profession",
            "svalue3", "svalue4", "raised_time", "", "nvalue6", "code",
            "svalue1", "nvalue5", "km_mark", "", "nvalue1", "nvalue2", "svalue2", "nvalue3", "nvalue4", "severity", "", "", "",
            "", "", "", "", "nvalue11", "nvalue12", "", "", "", "", "", "", "", "", "", "", "line_name", "direction",
            "power_section_name"};
    // 临时表和access数据库字段一对对应：svalue3 svalue4 RunDate RunTime nvalue6 code svalue1
    // nvalue5 maxpost maxminor
    // nvalue1 nvalue2 svalue2 nvalue3 nvalue4 severity valid frompost fromminor
    // topost tominor frtspeed passpeed nvalue11 nvalue12 indexforreportsummary
    // maxval1units maxval2units fromlatitudelongitude tolatitudelongitude
    // maxlatitudelongitude trackclass trackid defectfamily RunID

    public static boolean save(List<List<String>> metaNames, List<List<String>> data, MyTask task) {
        task.log("defects表数据总量:" + data.size());
        Log.log.writeLog(0, "defects数据总量:" + data.size());
        StringBuilder sql = new StringBuilder("insert into alarm_copy1 (");
        for (String field : fields) {
            if ("".equals(field)) {
                continue;
            }
            sql.append(field).append(",");
        }
        sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + ") values ");
        // (valueA1,valueA2,...valueAN),(valueB1,valueB2,...valueBN)
        for (List<String> rowData : data) {
            sql.append("(\"").append(UUID.randomUUID().toString()).append("\"").append(",");
            Util.addBaseInfo(sql).append("\"GW\",");
            // 排除前几列，append的时候索引需要-1
            for (int i = 4; i < fields.length; i++) {
                if ("".equals(fields[i])) {
                    continue;
                }
                /*
                 * i=3时为raised_time字段所需要的值，该数据是access里面的RunDate RunTime组合 为 yyyy-mm-dd
                 * hh-mm-ss格式 // 2019-11-27 14:10:03 <br> RunDate 2019/8/30 00:00:00.0000000
                 * RunTime 20:23:04
                 */
                if (i == 6) {
                    String runDataStr = rowData.get(i - 4);
                    String runTimeStr = rowData.get(i-3);
                    sql.append("\"").append(Util.getDataTime(runDataStr, runTimeStr)).append("\"").append(",");
                    continue;
                }

                //km_mark拼接
                if (i == 12) {
                    String maxPost = rowData.get(i - 4);
                    String maxMinor = rowData.get(i - 3);
                    sql.append("\"").append(Integer.parseInt(maxPost) * 1000 + maxMinor).append("\"").append(",");
                    continue;
                }

                if (rowData.get(i - 4) == null) {
                    sql.append("NULL,");
                } else {
                    sql.append("\"").append(rowData.get(i - 4)).append("\"").append(",");
                }
            }
            sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + "),");
        }
        String resultSql = sql.toString().substring(0, sql.toString().length() - 1) + ";";
        //System.out.println(resultSql);
        Connection connection = MySqlUtil.getConnection();
        synchronized (connection) {
            try {
                if (connection.isClosed()) {
                    return false;
                }
                Statement statement = connection.createStatement();
                connection.setAutoCommit(false);
                int num = statement.executeUpdate(resultSql);
                connection.commit();
                statement.close();
                task.log("defects值插入" + num + "条成功!");
                Log.log.writeLog(0, "defects值插入" + num + "条成功!");
                sum += num;
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                MySqlUtil.rollback(connection);
                Log.log.writeLog(-1, "数据插入异常，已回滚\n" + ExceptionUtil.getExceptionInfo(e));
            } catch (Exception e) {
                e.printStackTrace();
                MySqlUtil.rollback(connection);
                System.out.println("连接为空或者已关闭!" + e.getMessage());
            }
        }
        return false;
    }

}

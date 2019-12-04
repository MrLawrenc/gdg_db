package application.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import application.db.MySqlUtil;
import application.utils.ExceptionUtil;
import application.utils.Log;
import application.utils.MyTask;

/**
 * 对应表 tvalue
 *
 * @author mrliu
 */
public class TValueUtil {

    public static long sum = 0;

    public static boolean save( List<List<String>> data, MyTask task) {
        task.log("tvalue数据量:" + data.size());
        Log.log.writeLog(0, "tvalue数据量:" + data.size());
        StringBuilder sql = new StringBuilder("insert into rpt_gw_tvalue (");
        // for (String name : metaNames.get(0)) {
        // log(name);
        // }

        //其余固定信息字段
        sql.append("id,bureau_name,bureau_code,km_mark,");
        sql.append("post").append(",");
        sql.append("examine_std").append(",");
        sql.append("un_exceed_std").append(",");
        sql.append("exceed_std").append(",");
        sql.append("exceed_std_10").append(",");
        sql.append("exceed_std_20").append(",");
        sql.append("t_value").append(",");
        sql.append("line_name").append(",");
        sql.append("direction").append(",");
        sql.append("power_section_name");
        sql.append(") values ");

        // (valueA1,valueA2,...valueAN),(valueB1,valueB2,...valueBN)
        for (List<String> rowData : data) {
            sql.append("(\"").append(UUID.randomUUID().toString()).append("\",\"太原铁路局\",\"TYJ$J04\",\"")
                    .append(Integer.parseInt(rowData.get(0)) * 1000).append("\",");
            sql.append(rowData.get(0)).append(",");
            sql.append(rowData.get(1)).append(",");
            sql.append(rowData.get(2)).append(",");
            sql.append(rowData.get(3)).append(",");
            sql.append(rowData.get(4)).append(",");
            sql.append(rowData.get(5)).append(",");
            sql.append("\"").append(rowData.get(6)).append("\",");
            sql.append("\"").append(rowData.get(7)).append("\",");
            sql.append("\"").append(rowData.get(8)).append("\",");
            sql.append("\"").append(rowData.get(9)).append("\"),");
        }

        String resultSql = sql.toString().substring(0, sql.toString().length() - 1) + ";";
        Connection connection = null;
        try {
            connection = MySqlUtil.getConn0();
            Statement statement = connection.createStatement();
            connection.setAutoCommit(false);
            int num = statement.executeUpdate(resultSql);
            connection.commit();
            statement.close();
            task.log("tvalue值插入" + num + "条成功!");
            Log.log.writeLog(0, "tvalue值插入" + num + "条成功!");
            sum += num;
            MySqlUtil.returnConn(connection);
            return true;
        } catch (SQLException e) {
            MySqlUtil.rollback(connection);
            e.printStackTrace();
            Log.log.writeLog(-1, "数据插入异常，已回滚\n" + ExceptionUtil.getExceptionInfo(e));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取连接失败!" + e.getMessage());
        }
        MySqlUtil.returnConn(connection);
        return false;
    }
}

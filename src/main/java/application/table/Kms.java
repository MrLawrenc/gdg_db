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

public class Kms {

    public static long sum = 0;
    /**
     * 数据库字段
     */
    private static String[] fields = new String[]{"post", "length", "speedpass", "severity", "surface_4", "surface_3",
            "surface_2", "surface_1", "alignment_4", "alignment_3", "alignment_2", "alignment_1", "gauge_4", "gauge_3",
            "gauge_2", "gauge_1", "crosslevel_4", "crosslevel_3", "crosslevel_2", "crosslevel_1", "twist_4", "twist_3",
            "twist_2", "twist_1", "hori_acceleration_4", "hori_acceleration_3", "hori_acceleration_2",
            "hori_acceleration_1", "vert_acceleration_4", "vert_acceleration_3", "vert_acceleration_2",
            "vert_acceleration_1", "other", "surface_704", "surface_703", "surface_702", "surface_701", "alignment_704",
            "alignment_703", "alignment_702", "alignment_701", "curvature_rate_2", "curvature_rate_1", "gauge_rate_2",
            "gauge_rate_1", "land_accele_rate_2", "land_accele_rate_1", "recombin_rough_2", "recombin_rough_1",
            "land_acceleration_4", "land_acceleration_3", "land_acceleration_2", "land_acceleration_1",
            "l_corrugation_4", "l_corrugation_3", "l_corrugation_2", "l_corrugation_1", "r_rcorrugation_4",
            "r_rcorrugation_3", "r_rcorrugation_2", "r_rcorrugation_1", "line_name", "direction",
            "power_section_name"};

    public static boolean save( List<List<String>> data, MyTask task) {
        task.log("kms表数据总量:" + data.size());
        Log.log.writeLog(0, "kms数据总量:" + data.size());
        StringBuilder sql = new StringBuilder("insert into rpt_gw_kmscore (");
        //其余固定信息字段
        sql.append("id,bureau_name,bureau_code,km_mark,");
        for (String field : fields) {
            sql.append(field).append(",");
        }
        sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + ") values ");

        for (List<String> rowData : data) {
            //其余固定信息字段值
            sql.append("(\"").append(UUID.randomUUID().toString()).append("\",\"太原铁路局\",\"TYJ$J04\",\"")
                    .append(Integer.parseInt(rowData.get(0)) * 1000).append("\",");
            for (int i = 0; i < fields.length; i++) {
                sql.append("\"").append(rowData.get(i)).append("\",");
            }
            sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + "),");
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
            task.log("kms值插入" + num + "条成功!");
            Log.log.writeLog(0, "kms值插入" + num + "条成功!");
            sum += num;
            MySqlUtil.returnConn(connection);
            return true;
        } catch (SQLException e) {
            MySqlUtil.rollback(connection);
            Log.log.writeLog(-1, "数据插入异常，已回滚\n" + ExceptionUtil.getExceptionInfo(e));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取连接失败!" + e.getMessage());
        }
        MySqlUtil.returnConn(connection);
        return false;
    }
}

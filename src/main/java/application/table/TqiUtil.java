package application.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import application.db.MySqlUtil;
import application.utils.ExceptionUtil;
import application.utils.Log;
import application.utils.MyTask;

/**
 * tqi
 * 
 * @author mrliu
 */
public class TqiUtil {
public static long sum=0;
	public static boolean save(List<List<String>> metaNames, List<List<String>> data, MyTask task) {
		task.log("tqi数据总量:" + data.size());
		Log.log.writeLog(0, "tqi数据总量:" + data.size());
		StringBuilder sql = new StringBuilder("insert into rpt_gw_tqi (");

		// RecordNumber SubCode RunDate RunTime FromPost FromMinor TQIMetricName
		// TQIValue BasePost TrackID RunID

		sql.append("RecordNumber").append(",");
		sql.append("SubCode").append(",");
		sql.append("RunDate").append(",");
		sql.append("RunTime").append(",");
		sql.append("FromPost").append(",");
		sql.append("FromMinor").append(",");
		sql.append("TQIMetricName").append(",");
		sql.append("TQIValue").append(",");
		sql.append("BasePost").append(",");
		sql.append("RunID").append(",");
		sql.append("TrackID").append(",");
		sql.append("line_name").append(",");
		sql.append("direction").append(",");
		sql.append("power_section_name");

		sql.append(") values ");

		// (valueA1,valueA2,...valueAN),(valueB1,valueB2,...valueBN)
		for (int j = 0; j < data.size(); j++) {
			List<String> rowData = data.get(j);
			sql.append("(");
			sql.append("\"").append(rowData.get(0)).append("\",");
			sql.append("\"").append(rowData.get(1)).append("\",");
			sql.append("\"").append(rowData.get(2)).append("\",");
			sql.append("\"").append(rowData.get(3)).append("\",");
			sql.append("\"").append(rowData.get(4)).append("\",");
			sql.append("\"").append(rowData.get(5)).append("\",");
			sql.append("\"").append(rowData.get(6)).append("\",");
			sql.append("\"").append(rowData.get(7)).append("\",");
			sql.append("\"").append(rowData.get(8)).append("\",");
			sql.append("\"").append(rowData.get(9)).append("\",");
			sql.append("\"").append(rowData.get(10)).append("\",");
			sql.append("\"").append(rowData.get(11)).append("\",");
			sql.append("\"").append(rowData.get(12)).append("\",");

			sql.append("\"").append(rowData.get(13)).append("\"),");
		}
		String resultSql = sql.toString().substring(0, sql.toString().length() - 1) + ";";
		// log.detailLog("sql:" + resultSql.substring(200));
		Connection connection = MySqlUtil.getConnection();
		try {
			synchronized (connection) {
				if (connection.isClosed()) {
					Log.log.writeLog(1, "tvalue:连接关闭");
					return false;
				}
				Statement statement = connection.createStatement();
				connection.setAutoCommit(false);
				int num = statement.executeUpdate(resultSql);
				connection.commit();
				statement.close();

				task.log("tqi值插入" + num + "条成功!");
				Log.log.writeLog(0, "tqi值插入" + num + "条成功!");
				sum += num;
				return true;
			}
		} catch (SQLException e) {
			MySqlUtil.rollback(connection);
			Log.log.writeLog(-1, "数据插入异常，已回滚\n" + ExceptionUtil.getExceptionInfo(e));
		} catch (Exception e) {
			e.printStackTrace();
			MySqlUtil.rollback(connection);
			System.out.println("连接为空或者已关闭!" + e.getMessage());
		}
		return false;
	}
}

package application.table;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import application.db.MySqlUtil;
import application.utils.ExceptionUtil;
import application.utils.Log;
import application.utils.MyTask;

public class Kms {

	public static long sum=0;
	/**
	 * 数据库字段
	 */
	private static String[] fields = new String[] { "post", "length", "speedpass", "severity", "surface_4", "surface_3",
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
			"power_section_name" };

	public static boolean save(List<List<String>> metaNames, List<List<String>> data, MyTask task) {
		task.log("kms表数据总量:" + data.size());
		Log.log.writeLog(0, "kms数据总量:" + data.size());
		StringBuilder sql = new StringBuilder("insert into rpt_gw_kmscore (");
		for (int i = 0; i < fields.length; i++) {
			sql.append(fields[i]).append(",");
		}
		sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + ") values ");
		// (valueA1,valueA2,...valueAN),(valueB1,valueB2,...valueBN)
		for (int j = 0; j < data.size(); j++) {
			List<String> rowData = data.get(j);
			sql.append("(");
			for (int i = 0; i < fields.length; i++) {
				sql.append("\"").append(rowData.get(i)).append("\"").append(",");
			}
			sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + "),");
		}
		String resultSql = sql.toString().substring(0, sql.toString().length() - 1) + ";";
		// log.detailLog("sql:" + resultSql.substring(0, 200));
		Connection connection = MySqlUtil.getConnection();
		try {
			synchronized (connection) {
				if (connection.isClosed()) {
					return false; 
				}
				Statement statement = connection.createStatement();
				connection.setAutoCommit(false);
				int num = statement.executeUpdate(resultSql);
				connection.commit();
				statement.close();
				task.log("kms值插入" + num + "条成功!");
				Log.log.writeLog(0, "kms值插入" + num + "条成功!");
				sum += num;
				return true;
			}
		} catch (SQLException e) {
			MySqlUtil.rollback(connection);
			Log.log.writeLog(-1, "数据插入异常，已回滚\n" + ExceptionUtil.getExceptionInfo(e));
		} catch (Exception e) {
			MySqlUtil.rollback(connection);
			e.printStackTrace();
			System.out.println("连接为空或者已关闭!" + e.getMessage());
		}
		return false;
	}
}

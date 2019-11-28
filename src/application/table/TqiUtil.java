package application.table;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import application.db.MySqlUtil;
import application.utils.ExceptionUtil;
import application.utils.MyTask;

/**
 * tqi
 * 
 * @author mrliu
 */
public class TqiUtil {

	public static void save0(List<List<String>> metaNames, List<List<String>> data, MyTask task) {

//		int limit = 20000;
//		if (data.size() > limit) {
//			int temp = data.size() / limit;
//			for (int i = 0; i < temp; i++) {
//				log.infoLog("执行:" + i * limit + " 到 " + (i + 1) * limit);
//				save(metaNames, data.subList(i * limit, (i + 1) * limit), url);
//			}
//			log.infoLog("执行:" + temp * limit + " 到 " + data.size());
//			save(metaNames, data.subList(temp * limit, data.size()), url);
//		} else {
//			save(metaNames, data, url);
//		}
		save(metaNames, data, task);

	}

	public static void save(List<List<String>> metaNames, List<List<String>> data, MyTask task) {
		task.log("tqi数据总量:" + data.size());
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
		try {
			Statement statement = MySqlUtil.getConnection().createStatement();
			long num=statement.executeUpdate(resultSql);
			task.log("tqi值插入"+num+"条成功!");
		} catch (SQLException e) {
			task.log("0" + ExceptionUtil.appendExceptionInfo(e));
		}
	}
}

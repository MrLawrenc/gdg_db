package application.table;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import application.db.MySqlUtil;
import application.utils.ExceptionUtil;
import application.utils.MyTask;

/**
 * 对应表 tvalue
 * 
 * @author mrliu
 */
public class TValueUtil {

	public static void save(List<List<String>> metaNames, List<List<String>> data, MyTask task) {
		task.log("tvalue数据量:" + data.size());
		StringBuilder sql = new StringBuilder("insert into rpt_gw_tvalue (");
		// for (String name : metaNames.get(0)) {
		// log(name);
		// }

		sql.append("km_mark").append(",");
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
		for (int j = 0; j < data.size(); j++) {
			List<String> rowData = data.get(j);
			sql.append("(");
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
		// log.detailLog("sql:" + resultSql.substring(200));
		try {
//			 PreparedStatement prepareStatement = MySqlUtil.getConnection(url).prepareStatement("");
//			 prepareStatement.addBatch("");
//			 prepareStatement.execute("");

			Statement statement = MySqlUtil.getConnection().createStatement();
			statement.execute(resultSql);
			task.log("tvalue值插入"+0+"条成功!");
		} catch (SQLException e) {
			task.log("0" + ExceptionUtil.getExceptionInfo(e));
		}
	}
}

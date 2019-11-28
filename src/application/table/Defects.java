package application.table;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLType;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import application.db.MySqlUtil;
import application.utils.ExceptionUtil;
import application.utils.Log;
import application.utils.MyTask;

public class Defects {
	// alarm表需要的字段，
	// RunTime,bureau_code,bureau_name,power_section_code,power_section_name,profession,id,svalue12,
	// line_code,line_name,raised_time,km_mark,created_time,code_name,direction,
	// nvalue12,nvalue11, severity,
	// nvalue4,nvalue3,svalue2,nvalue2,nvalue1,
	// nvalue5,svalue1,code,nvalue6,svalue4,svalue3,
	/**
	 * 部分数据库不需要的字段为“”，后面会直接跳过
	 */
	private static String[] fields = new String[]{"id", "svalue3", "svalue4", "raised_time", "",
			"nvalue6", "code", "svalue1", "nvalue5", "", "", "nvalue1", "nvalue2", "svalue2",
			"nvalue3", "nvalue4", "severity", "", "", "", "", "", "", "", "nvalue11", "nvalue12",
			"", "", "", "", "", "", "", "", "", "", "line_name", "direction", "power_section_name"};
	// 临时表和access数据库字段一对对应：svalue3 svalue4 RunDate RunTime nvalue6 code svalue1
	// nvalue5 maxpost maxminor
	// nvalue1 nvalue2 svalue2 nvalue3 nvalue4 severity valid frompost fromminor
	// topost tominor frtspeed passpeed nvalue11 nvalue12 indexforreportsummary
	// maxval1units maxval2units fromlatitudelongitude tolatitudelongitude
	// maxlatitudelongitude trackclass trackid defectfamily RunID

	public static void save(List<List<String>> metaNames, List<List<String>> data, MyTask task) {
		task.log("defects表数据总量:" + data.size());
		
		StringBuilder sql = new StringBuilder("insert into alarm (");
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].equals("")) {
				continue;
			}
			sql.append(fields[i]).append(",");
		}
		sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + ") values ");
		// (valueA1,valueA2,...valueAN),(valueB1,valueB2,...valueBN)
		for (int j = 0; j < data.size(); j++) {
			List<String> rowData = data.get(j);
			sql.append("(");
			sql.append("\"").append(UUID.randomUUID().toString()).append("\"").append(",");
			// 排除id列，append的时候索引需要-1
			for (int i = 1; i < fields.length; i++) {
				if (fields[i].equals("")) {
					continue;
				}
				/*
				 * i=3时为raised_time字段所需要的值，该数据是access里面的RunDate RunTime组合 为 yyyy-mm-dd
				 * hh-mm-ss格式 // 2019-11-27 14:10:03 <br> RunDate 2019/8/30 00:00:00.0000000
				 * RunTime 20:23:04
				 */
				if (i == 3) {
					String result = "";
					String runDataStr = rowData.get(i - 1);
					String runTimeStr = rowData.get(i);
					if (StringUtils.isEmpty(runDataStr)) {
						result = "2019-11-27 14:10:03";
					} else {
						String resultRunData = runDataStr.trim().replaceAll("/", "-").substring(0,
								10);
						if (StringUtils.isEmpty(runDataStr)) {
							result = resultRunData + " 14:10:03";
						} else {
							result = resultRunData + " " + runTimeStr.trim();
						}
					}
					sql.append("\"").append(result).append("\"").append(",");
					// task.log("1alarm table raise_time:" + result);
					continue;
				}

				if (rowData.get(i - 1) == null) {
					sql.append("NULL,");
				} else {
					sql.append("\"").append(rowData.get(i - 1)).append("\"").append(",");
				}
			}
			sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + "),");
		}
		String resultSql = sql.toString().substring(0, sql.toString().length() - 1) + ";";
		
		// log.detailLog(resultSql.substring(0, 300));
		try {
			long currentTimeMillis = System.currentTimeMillis();
			Connection connection = MySqlUtil.getConnection();
			Statement statement = connection.createStatement();
			connection.setAutoCommit(false);
			int num = statement.executeUpdate(resultSql);
			connection.commit();
			statement.close();
			task.log("defects值插入" + num + "条成功!");
			Log.log.writeLog(0, "defects值插入" + num + "条成功!");
			System.out.println("defects耗时:"+(System.currentTimeMillis() - currentTimeMillis));
		} catch (SQLException e) {
			Log.log.writeLog(-1, ExceptionUtil.getExceptionInfo(e));
		}
	}
	public static void save0(List<List<String>> metaNames, List<List<String>> data, MyTask task) {
		task.log("defects表数据总量:" + data.size());
		StringBuilder sql = new StringBuilder("insert into alarm (");
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].equals("")) {
				continue;
			}
			sql.append(fields[i]).append(",");
		}
		sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + ") values (");
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].equals("")) {
				continue;
			}
			sql.append("?,");
		}
		sql = new StringBuilder(sql.substring(0, sql.toString().length() - 1) + ");");
		System.out.println(sql);
		Connection connection = MySqlUtil.getConnection();
		try {
			PreparedStatement pst = connection.prepareStatement(sql.toString());
			connection.setAutoCommit(false);
			// (valueA1,valueA2,...valueAN),(valueB1,valueB2,...valueBN)
			for (int j = 0; j < data.size(); j++) {
				List<String> rowData = data.get(j);
				int tempIndex = 1;
				pst.setString(tempIndex++, UUID.randomUUID().toString());
				// 排除id列，append的时候索引需要-1
				for (int i = 1; i < fields.length; i++) {
					if (fields[i].equals("")) {
						continue;
					}
					/*
					 * i=3时为raised_time字段所需要的值，该数据是access里面的RunDate RunTime组合 为 yyyy-mm-dd
					 * hh-mm-ss格式 // 2019-11-27 14:10:03 <br> RunDate 2019/8/30 00:00:00.0000000
					 * RunTime 20:23:04
					 */
					if (i == 3) {
						String result = "";
						String runDataStr = rowData.get(i - 1);
						String runTimeStr = rowData.get(i);
						if (StringUtils.isEmpty(runDataStr)) {
							result = "2019-11-27 14:10:03";
						} else {
							String resultRunData = runDataStr.trim().replaceAll("/", "-")
									.substring(0, 10);
							if (StringUtils.isEmpty(runDataStr)) {
								result = resultRunData + " 14:10:03";
							} else {
								result = resultRunData + " " + runTimeStr.trim();
							}
						}
						pst.setString(tempIndex++, result);
						continue;
					}

					if (rowData.get(i - 1) == null) {
						pst.setNull(tempIndex++, Types.DECIMAL);
					} else {
						pst.setString(tempIndex++, rowData.get(i - 1));
					}
				}
				pst.addBatch();
			}
			long currentTimeMillis = System.currentTimeMillis();

			int[] num = pst.executeBatch();
			System.out.println(num);
			connection.commit();
			pst.close();
			System.out.println(System.currentTimeMillis() - currentTimeMillis);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}

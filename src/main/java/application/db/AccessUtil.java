package application.db;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : LiuMingyao
 * @date : 2019/11/22 13:54
 * @description : TODO
 */
@SuppressWarnings("all")
public class AccessUtil {
	/**
	 * 所有数据库文件的顶层目录
	 */
	// private static String parentFilePath =
	// "E:\\工电供资料\\document\\6.客户资料\\工务\\工务检测数据\\2019年第三季度综合检测车联检";
	private static String parentFilePath = "F:\\gtdq\\gtdq_gdg\\lib";
	/**
	 * 文件后缀
	 */
	private static String fileSuffix1 = ".iic";
	private static String fileSuffix2 = ".mdb";

	public static void closeConn(String dbPath) throws Exception {
		Connection connection = connectionMap.get(dbPath);
		if (connection != null) {
			connection.close();
			connectionMap.remove(dbPath);
		}
	}

	private static void connectDb(String dbPath) throws Exception {
		// iic和mdb文件都可以
		String accessUrl = "jdbc:ucanaccess://" + dbPath;
		Connection conn = DriverManager.getConnection(accessUrl);
		Statement s = conn.createStatement();

		ResultSet resultSet = conn.getMetaData().getTables(null, null, "%", null);
		while (resultSet.next()) {
			String tableName = resultSet.getString(3);
			System.out.println("table-name:" + tableName + "");

			ResultSet rs = s.executeQuery("SELECT * FROM " + tableName);
			ResultSetMetaData metaData = rs.getMetaData();
			String clm = "";
			for (int i = 0; i < metaData.getColumnCount(); i++) {
				clm += metaData.getColumnName(i + 1) + "\t";
			}
			System.out.println(clm);
			while (rs.next()) {
				String value = "";
				for (int i = 0; i < metaData.getColumnCount(); i++) {
					value += rs.getString(i + 1) + "\t";
				}
				System.out.println(value);
			}
		}

	}

	public static void main(String[] args) throws Exception {
		File topParent = new File(parentFilePath);
		if (!topParent.isDirectory()) {
			System.out.println("目标文件不是文件夹.................");
			return;
		}
		ArrayList<File> files = accordRequireFiles(topParent);
		for (int i = 0; i < 1; i++) {
			String dbPath = files.get(i).getAbsolutePath();
			List<String> tableNames = allTableName(dbPath);
			tableNames.forEach(tableName -> {
				try {
					System.out.println("=====================================" + tableName
							+ "=====================================");
					getTableDataByTableName(dbPath, tableName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	/**
	 * 获取该数据库dbPath下的 该表tableName的所有数据 外层list的0处索引是表头信息，其余都是数据，每个内层list都是一行数据
	 * 
	 * @param dbPath
	 * @param tableName
	 * @throws Exception
	 */
	public static List<List<String>> getTableDataByTableName(String dbPath, String tableName) throws Exception {
		Connection connection = getConn(dbPath);
		ResultSet rs = connection.createStatement().executeQuery("SELECT * FROM " + tableName);
		ResultSetMetaData metaData = rs.getMetaData();

		List<String> fieldName = new ArrayList<String>();
		String clm = "";
		for (int i = 0; i < metaData.getColumnCount(); i++) {
			fieldName.add(metaData.getColumnName(i + 1));
			clm += metaData.getColumnName(i + 1) + "\t";
		}
		// System.out.println("调试信息:表头\t\t" + clm);
		List<List<String>> result = new ArrayList<List<String>>();
		result.add(fieldName);
		while (rs.next()) {
			String value = "";
			// 每一行记录
			List<String> rowList = new ArrayList<String>(metaData.getColumnCount());
			for (int i = 0; i < metaData.getColumnCount(); i++) {
				String cloumnValue = rs.getString(i + 1);
				rowList.add(cloumnValue);
				value += rs.getString(i + 1) + "\t";
			}
			result.add(rowList);
			// System.out.println("调试信息，每行记录：" + value);
		}
		return result;
	}

	/**
	 * 连接缓存，key为每一个数据库的path，value为connection连接对象
	 */
	private static Map<String, Connection> connectionMap = new HashMap<>();

	/**
	 * 获取access数据库连接对象
	 */
	private static Connection getConn(String dbPath) {
		Connection conn = connectionMap.get(dbPath);
		try {
			if (conn == null) {
				// iic和mdb文件都可以
				String accessUrl = "jdbc:ucanaccess://" + dbPath;
				conn = DriverManager.getConnection(accessUrl);
				connectionMap.put(dbPath, conn);
			}
			return conn;
		} catch (SQLException e) {
			System.out.println("获取连接失败");
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}

	/**
	 * 获取该数据库的所有表名
	 *
	 * @param dbPath 数据库文件所在目录
	 * @return 该数据的所有表名
	 */
	public static List<String> allTableName(String dbPath) throws Exception {

		Connection conn = getConn(dbPath);

		ResultSet resultSet = conn.getMetaData().getTables(null, null, "%", null);
		List<String> names = new ArrayList<>();
		while (resultSet.next()) {
			String tableName = resultSet.getString(3);
			// System.out.println("table-name: " + tableName);
			names.add(tableName);
		}
		return names;
	}

	/**
	 * 根据后缀查找所有文件
	 */
	public static void searchFileBySuffix(String parentFilePath, String fileSuffix) {
		parentFilePath = "E:\\工电供资料\\document\\6.客户资料\\工务\\工务检测数据\\2019年第三季度综合检测车联检";
		fileSuffix = ".iic";
		File topParent = new File(parentFilePath);
		if (!topParent.isDirectory()) {
			System.out.println("目标文件不是文件夹.................");
			return;
		}
		ArrayList<File> files = accordRequireFiles(topParent);
		for (int i = 0; i < 2; i++) {
			File file = files.get(i);

		}
	}

	/**
	 * 返回符合要求的所有文件集合
	 */
	public static ArrayList<File> accordRequireFiles(File topParent) {
		ArrayList<File> fileResult = new ArrayList<>();
		addFile(topParent, fileResult);
		return fileResult;
	}

	/**
	 * 递归添加所有文件到指定集合
	 */
	private static void addFile(File parentFile, List<File> result) {
		File[] files = parentFile.listFiles();
		if (files == null) {
			return;
		}
		for (File currentFile : files) {
			if (currentFile.isFile()) {
				String fileName = currentFile.getName();
				String suffix = fileName.substring(fileName.length() - 4);
				if (fileSuffix1.equals(suffix) || fileSuffix2.equals(suffix)) {
					result.add(currentFile);
				}
			} else {
				addFile(currentFile, result);
			}
		}
	}
}
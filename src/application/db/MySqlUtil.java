package application.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("all")
public class MySqlUtil {

	// private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

	private static Map<Integer, Connection> connMap;
	private static List<Connection> connections = Collections.synchronizedList(new ArrayList<>(10));

	static {
		try {
			Class.forName(DRIVER);
			connMap = new HashMap<Integer, Connection>();
		} catch (ClassNotFoundException e) {
			System.err.println("can not load jdbc driver");
			e.printStackTrace();
		}
	}

	public static Connection getConn() {
		while (connections.size() == 0) {
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		return connections.get(0);
	}

	public static void setConnections(List<Connection> connections) {
		MySqlUtil.connections = connections;
	}

	private static int flag = 0;

	public static Connection getConnection() {
		// 等待异步的mysql连接全部初始化完毕
		while (10 != connNum) {
			try {
				TimeUnit.MILLISECONDS.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (flag == 9) {
			flag = 0;
			return connMap.get(0);
		}
		return connMap.get(++flag);
	}

	public static void closeConnection() {
		if (connMap != null) {
			connMap.values().forEach(conn -> {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		connMap.clear();
	}

	private static int connNum = 10;

	public static void initConn(String url, String username, String pwd) {
		try {
			for (int i = 0; i < connNum; i++) {
				Connection connection = DriverManager.getConnection(url, username, pwd);
				connMap.put(i, connection);
			}
		} catch (Exception e) {
			System.err.println("get mysql connection failure");
			e.printStackTrace();
		}
	}

}
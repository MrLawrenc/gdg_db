package application.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import application.utils.Log;

@SuppressWarnings("all")
public class MySqlUtil {

	// private static final String DRIVER = "com.mysql.jdbc.Driver";
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

	private volatile static Map<Integer, Connection> connMap;
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

	private volatile static int flag = 0;

	public synchronized static Connection getConnection() {
		// 等待异步的mysql连接全部初始化完毕
		while (!done.get()) {
			try {
				System.out.println("mysql连接还未就绪，等待连接....................");
				Log.log.writeLog(1, "mysql连接还未就绪，等待连接....................");
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

	/**
	 * 加锁，防止事务回滚
	 */
	public static void closeConnection() {
		if (connMap != null) {
			Log.log.writeLog(1, "准备关闭所有mysql连接!");
			connMap.values().forEach(conn -> {
				synchronized (conn) {
					try {
						conn.close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			Log.log.writeLog(1, "所有mysql连接关闭完成!");
		}
		connMap.clear();
	}

	private static int connNum = 10;
	private static AtomicBoolean done = new AtomicBoolean(false);

	public static void initConn(String url, String username, String pwd) {
		try {
			for (int i = 0; i < connNum; i++) {
				Connection connection = DriverManager.getConnection(url, username, pwd);
				connMap.put(i, connection);
			}
			done.compareAndSet(false, true);
		} catch (Exception e) {
			System.err.println("get mysql connection failure");
			e.printStackTrace();
		}
	}

	/**
	 * 需要对回滚的连接进行加锁，防止connection在回滚的同时关闭
	 * 
	 * @param connection
	 */
	public static void rollback(Connection connection) {
		synchronized (connection) {

			try {
				if (connection.isClosed()) {
					return;
				}
				connection.rollback();
				Log.log.writeLog(0, "事务回滚成功！");
				System.out.println("事务回滚成功");

			} catch (SQLException e1) {
				System.out.println("回滚失败。。。。。。。。。。。");
				e1.printStackTrace();
			}

		}
	}
}
package application.db;

import application.utils.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * 从连接池中获取连接，轮训方式，不需要手动归还，也可以使用队列
     */
    public synchronized static Connection getConnection() {
        long start = System.currentTimeMillis();
        // 等待异步的mysql连接全部初始化完毕
        while (!done.get()) {
            //10s拿不到就退出
            if (System.currentTimeMillis() - start > 10000) {
                return null;
            }
            try {
                System.out.println("mysql连接还未就绪，等待连接....................");
                Log.log.writeLog(1, "mysql连接还未就绪，等待连接....................");
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
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
                        if (!conn.isClosed()) {
                            conn.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
            Log.log.writeLog(1, "所有mysql连接关闭完成!");
        }
        connMap.clear();
    }

    private static int connNum = 10;
    /**
     * 所有连接是否初始化完毕
     */
    private static AtomicBoolean done = new AtomicBoolean(false);

    /**
     * 生成手动提交的连接池
     */
    public static void initConn(String url, String username, String pwd) throws Exception {
        try {
            for (int i = 0; i < connNum; i++) {
                Connection connection = DriverManager.getConnection(url, username, pwd);
                connection.setAutoCommit(false);
                connMap.put(i, connection);
            }
            done.compareAndSet(false, true);
        } catch (Exception e) {
            System.err.println("get mysql connection failure");
            throw e;
        }
    }

    /**
     * 回滚，外层调用时加锁了的
     */
    public static void rollback(Connection connection) {
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
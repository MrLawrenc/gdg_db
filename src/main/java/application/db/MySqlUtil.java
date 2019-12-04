package application.db;

import application.utils.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author : LiuMing
 * @date : 2019/12/4 13:33
 * @description :   TODO
 */
@SuppressWarnings("all")
public class MySqlUtil {
    private static int poolSize = 10;
    private static ArrayBlockingQueue<Connection> connPool = new ArrayBlockingQueue<>(poolSize);


    /**
     * 初始化mysql连接池
     */
    public static void init(String url, String username, String pwd) {
        try {
            for (int i = 0; i < poolSize; i++) {
                Connection connection = DriverManager.getConnection(url, username, pwd);
                connection.setAutoCommit(false);
                connPool.add(connection);
                System.out.println("mysql以初始化的连接数:" + (i + 1));
            }
            System.out.println("mysql连接池全部初始化完毕。。。。。。。。。。。。");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取连接
     */
    public static Connection getConn0() throws InterruptedException {
        System.out.println("连接池剩余连接数："+connPool.size());
        //移除并返回头部元素，如果为空则阻塞
        return connPool.take();
    }

    /**
     * 归还连接
     */
    public static void returnConn(Connection connection) {
        //移除并返回头部元素，如果为空则阻塞
        connPool.add(connection);
    }

    /**
     * 关闭所有连接
     */
    public static void close0() {
        try {
            for (int i = 0; i < poolSize; i++) {
                Connection connection = connPool.take();
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            }
            System.out.println("mysql连接池关闭完成...........");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //old driver
    // private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    private volatile static Map<Integer, Connection> connMap;
    private static List<Connection> connections = Collections.synchronizedList(new ArrayList<>(10));

    static {
        try {
            Class.forName(DRIVER);
            connMap = new ConcurrentHashMap<>();
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
            try {
                System.out.println("mysql连接还未就绪，等待连接...................." + connMap.size());
                Log.log.writeLog(1, "mysql连接还未就绪，等待连接...................." + connMap.size());
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (flag == connNum - 1) {
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

    /**
     * 连接池数量
     */
    private static int connNum = 6;
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
            System.out.println("mysql连接初始化完毕................");
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
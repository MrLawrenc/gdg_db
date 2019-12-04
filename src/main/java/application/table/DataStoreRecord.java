package application.table;

import application.db.MySqlUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author : LiuMingyao
 * @date : 2019/12/4 13:21
 * @description : 对应记录表data_store_record表
 */
public class DataStoreRecord {
    private String[] fields = new String[]{"parent_file_name", "db_file_name", "table_name", "state", "create_time", "success_time"};

    private static String insertSql = "insert into data_store_record(parent_file_name,db_file_name,table_name,state,create_time)" +
            " values(?,?,?,0,?);";
    private static String updateSql = "update data_store_record set state=1,success_time=?" +
            " where parent_file_name=? and db_file_name=? and table_name=? ;";

    /**
     * 开始保存数据
     */
    public static void startSave(DataStoreRecordPo record) {
        Connection connection = null;
        try {
            connection = MySqlUtil.getConn0();
            PreparedStatement pst = connection.prepareStatement(insertSql);
            pst.setString(1, record.getParentFileName());
            pst.setString(2, record.getDbFileName());
            pst.setString(3, record.getTableName());
            pst.setDate(4, record.getCreateTime());
            pst.execute();
            connection.commit();
            MySqlUtil.returnConn(connection);
        } catch (SQLException e) {
            e.printStackTrace();
            MySqlUtil.rollback(connection);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("获取mysql连接失败!");
        }
    }

    /**
     * 保存成功，更改状态
     */
    public static void saveSuccess(DataStoreRecordPo record) {
        Connection connection = null;
        try {
            connection = MySqlUtil.getConn0();
            PreparedStatement pst = connection.prepareStatement(updateSql);
            pst.setDate(1, record.getSuccessTime());

            pst.setString(2, record.getParentFileName());
            pst.setString(3, record.getDbFileName());
            pst.setString(4, record.getTableName());

            pst.execute();
            connection.commit();
            MySqlUtil.returnConn(connection);
        } catch (SQLException e) {
            e.printStackTrace();
            MySqlUtil.rollback(connection);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("获取mysql连接失败!");
        }

    }
}
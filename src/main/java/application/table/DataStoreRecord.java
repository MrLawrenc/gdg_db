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
     * 开始保存数据,传入connection是保证{@link DataStoreRecord#startSave(Connection, DataStoreRecordPo)}和
     * {@link DataStoreRecord#saveSuccess(Connection, DataStoreRecordPo)}方法在同一个connection内完成，防止当
     * startSave执行成功之后，程序被用户关闭，可能出现保存成功但是未更新状态的情况，那么下次可能会重复入库。
     * 当使用同一个conn之后，在连接还未归还的时候程序并不会关闭，所以即使用户关闭界面，那么保存成功之后必会更新状态，之后再返还
     * connection对象，之后连接池关闭
     */
    public static void startSave(Connection connection, DataStoreRecordPo record) {
        try {
            PreparedStatement pst = connection.prepareStatement(insertSql);
            pst.setString(1, record.getParentFileName());
            pst.setString(2, record.getDbFileName());
            pst.setString(3, record.getTableName());
            pst.setDate(4, record.getCreateTime());
            pst.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            MySqlUtil.rollback(connection);
        }
    }

    /**
     * 保存成功，更改状态
     */
    public static void saveSuccess(Connection connection, DataStoreRecordPo record) {
        try {
            PreparedStatement pst = connection.prepareStatement(updateSql);
            pst.setDate(1, record.getSuccessTime());

            pst.setString(2, record.getParentFileName());
            pst.setString(3, record.getDbFileName());
            pst.setString(4, record.getTableName());

            pst.execute();
            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            MySqlUtil.rollback(connection);
        }
    }
}
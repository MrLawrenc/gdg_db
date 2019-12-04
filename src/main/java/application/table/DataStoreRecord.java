package application.table;

/**
 * @author : LiuMingyao
 * @date : 2019/12/4 13:21
 * @description : 对应记录表data_store_record表
 */
public class DataStoreRecord {
    private String[] fields = new String[]{"parent_file_name", "db_file_name", "table_name", "state", "create_time", "success_time"};


    /**
     * 开始保存数据
     */
    public static void startSave() {

    }

    /**
     * 保存成功，更改状态
     */
    public static void saveSuccess() {

    }
}
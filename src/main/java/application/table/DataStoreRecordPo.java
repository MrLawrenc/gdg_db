package application.table;


import java.sql.Timestamp;
import java.sql.Timestamp;

/**
 * @author : LiuMingyao
 * @date : 2019/12/4 13:19
 * @description : 记录入库信息模型
 */
public class DataStoreRecordPo {
    @Override
    public String toString() {
        return "DataStoreRecordPo{" +
                "parentFileName='" + parentFileName + '\'' +
                ", dbFileName='" + dbFileName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", state=" + state +
                ", createTime=" + createTime +
                ", successTime=" + successTime +
                '}';
    }

    private String parentFileName;
    private String dbFileName;
    private String tableName;
    private int state;
    private Timestamp createTime;
    private Timestamp successTime;

    public String getParentFileName() {
        return parentFileName;
    }

    public void setParentFileName(String parentFileName) {
        this.parentFileName = parentFileName;
    }

    public String getDbFileName() {
        return dbFileName;
    }

    public void setDbFileName(String dbFileName) {
        this.dbFileName = dbFileName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Timestamp successTime) {
        this.successTime = successTime;
    }

    public DataStoreRecordPo(String parentFileName, String dbFileName, String tableName) {
        this.parentFileName = parentFileName;
        this.dbFileName = dbFileName;
        this.tableName = tableName;
        this.state = 0;

        this.createTime = new Timestamp(System.currentTimeMillis());
    }

    public DataStoreRecordPo toSuccess() {
        this.state = 1;
        this.successTime = new Timestamp(System.currentTimeMillis());
        return this;
    }
}
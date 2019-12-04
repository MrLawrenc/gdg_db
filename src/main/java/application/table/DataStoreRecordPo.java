package application.table;


import java.sql.Date;

/**
 * @author : LiuMingyao
 * @date : 2019/12/4 13:19
 * @description : 记录入库信息模型
 */
public class DataStoreRecordPo {

    private String parentFileName;
    private String dbFileName;
    private String tableName;
    private int state;
    private Date createTime;
    private Date successTime;

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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(Date successTime) {
        this.successTime = successTime;
    }

    public DataStoreRecordPo(String parentFileName, String dbFileName, String tableName) {
        this.parentFileName = parentFileName;
        this.dbFileName = dbFileName;
        this.tableName = tableName;
        this.state = 0;
        this.createTime = new Date(System.currentTimeMillis());
    }

    public DataStoreRecordPo toSuccess() {
        this.state = 1;
        this.successTime = new Date(System.currentTimeMillis());
        return this;
    }
}
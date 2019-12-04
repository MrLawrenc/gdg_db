package application.table;

import java.time.LocalDateTime;

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
    private LocalDateTime createTime;
    private LocalDateTime successTime;

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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(LocalDateTime successTime) {
        this.successTime = successTime;
    }
}
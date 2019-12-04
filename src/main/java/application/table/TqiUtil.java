package application.table;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import application.db.MySqlUtil;
import application.utils.ExceptionUtil;
import application.utils.Log;
import application.utils.MyTask;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * tqi
 *
 * @author mrliu
 */
public class TqiUtil {
    public static long sum = 0;
    private static String[] fields = new String[]{"MAXSPEED", "MEANSPEED", "STDLATACCEL", "STDVERTACCEL", "TBCE",
            "STDSUMS", "EXCEEDED", "R_STDSURF", "L_STDSURF", "L_STDALIGN",
            "R_STDALIGN", "STDGAUGE", "STDTWIST", "STDCROSSLEVEL"};

    public static boolean save( List<List<String>> data, MyTask task) {
        task.log("tqi数据总量:" + data.size());
        Log.log.writeLog(0, "tqi数据总量:" + data.size());
        StringBuilder sql = new StringBuilder("insert into rpt_gw_tqi (");
        //step1.所有数据封装为对象
        List<TqiPo> tqiPoList = data.stream().map(rowData -> new TqiPo(rowData.get(0), rowData.get(1), rowData.get(2), rowData.get(3), rowData.get(4),
                rowData.get(5), rowData.get(6), rowData.get(7), rowData.get(8), rowData.get(9), rowData.get(10),
                rowData.get(11), rowData.get(12), rowData.get(13))).collect(toList());
        //step2.行转列
        List<TqiPo> resultData = new ArrayList<>();
        tqiPoList.stream().collect(groupingBy(TqiPo::getFromPost)).values().forEach(byPostGroup -> {
            Map<String, List<TqiPo>> byMinorGroup = byPostGroup.stream().collect(groupingBy(TqiPo::getFromMinor));
            byMinorGroup.values().forEach(needRow -> {
                TqiPo tqiPo = needRow.get(0);
                String fromPost = tqiPo.getFromPost();
                String fromMinor = tqiPo.getFromMinor();

                //或取行转列之后的每一列的值
                String[] tValues = new String[14];
                for (int i = 0; i < fields.length; i++) {
                    for (TqiPo po : needRow) {
                        if (fields[i].equals(po.gettQIMetricName())) {
                            tValues[i] = po.gettQIValue();
                        }
                    }
                }
                tqiPo.setTargetFields(tValues[0], tValues[1], tValues[2], tValues[3], tValues[4], tValues[5], tValues[6],
                        tValues[7], tValues[8], tValues[9], tValues[10], tValues[11], tValues[12], tValues[13]);

                int kmMark = Integer.parseInt(fromPost) * 1000 + Integer.parseInt(fromMinor);
                tqiPo.setKmMark(BigDecimal.valueOf(kmMark));
                resultData.add(tqiPo);
            });
        });
        //System.out.println("result:" + resultData.get(0));
        // RecordNumber SubCode RunDate RunTime FromPost FromMinor TQIMetricName
        // TQIValue BasePost TrackID RunID

        sql.append("id,bureau_name,bureau_code,RecordNumber,SubCode,RunDate,RunTime,raised_time,FromPost,FromMinor,km_mark,TQIMetricName,TQIValue,BasePost,TrackID," +
                "RunID,line_name,direction,power_section_name,");
        /* MAXSPEED;MEANSPEED STDLATACCEL  STDVERTACCEL  TBCE STDSUMS  EXCEEDED R_STDSURF L_STDSURF  L_STDALIGN
         *  R_STDALIGN  STDGAUGE  STDTWIST  STDCROSSLEVEL
         */
        sql.append("MAXSPEED,MEANSPEED,STDLATACCEL,STDVERTACCEL,TBCE,STDSUMS,EXCEEDED,R_STDSURF,L_STDSURF,L_STDALIGN," +
                "R_STDALIGN,STDGAUGE,STDTWIST,STDCROSSLEVEL) values ");

        // (valueA1,valueA2,...valueAN),(valueB1,valueB2,...valueBN)
        for (TqiPo rowData : resultData) {
            sql.append("(\"").append(UUID.randomUUID().toString()).append("\",");
            sql.append("\"太原铁路局\",");
            sql.append("\"TYJ$J04\",");
            sql.append("\"").append(rowData.getRecordNumber()).append("\",");
            sql.append("\"").append(rowData.getSubCode()).append("\",");
            sql.append("\"").append(rowData.getRunDate()).append("\",");
            sql.append("\"").append(rowData.getRunTime()).append("\",");

            //拼接时间
            if (rowData.getRunDate() == null) {
                sql.append("\"").append("2019-11-27 14:10:03").append("\",");
            } else {
                String resultRunData = rowData.getRunDate().replaceAll("/", "-").substring(0, 10);
                if (rowData.getRunTime() == null) {
                    sql.append("\"").append(resultRunData).append(" 00:00:00").append("\",");
                } else {
                    sql.append("\"").append(resultRunData).append(" ").append(rowData.getRunTime()).append("\",");
                }
            }

            sql.append("\"").append(rowData.getFromPost()).append("\",");
            sql.append("\"").append(rowData.getFromMinor()).append("\",");
            //km_mark
            sql.append("\"").append(Integer.parseInt(rowData.getFromMinor()) + Integer.parseInt(rowData.getFromPost()) * 1000).append("\",");

            sql.append("\"").append(rowData.gettQIMetricName()).append("\",");
            sql.append("\"").append(rowData.gettQIValue()).append("\",");
            sql.append("\"").append(rowData.getBasePost()).append("\",");
            sql.append("\"").append(rowData.getTrackID()).append("\",");
            sql.append("\"").append(rowData.getRunID()).append("\",");
            sql.append("\"").append(rowData.getLineName()).append("\",");
            sql.append("\"").append(rowData.getDirection()).append("\",");
            sql.append("\"").append(rowData.getPowerSectionName()).append("\",");
            //==========转换为列之后的数据
            sql.append("\"").append(rowData.getMAXSPEED()).append("\",");
            sql.append("\"").append(rowData.getMEANSPEED()).append("\",");
            sql.append("\"").append(rowData.getSTDLATACCEL()).append("\",");
            sql.append("\"").append(rowData.getSTDVERTACCEL()).append("\",");
            sql.append("\"").append(rowData.getTBCE()).append("\",");

            sql.append("\"").append(rowData.getSTDSUMS()).append("\",");
            sql.append("\"").append(rowData.getEXCEEDED()).append("\",");
            sql.append("\"").append(rowData.getR_STDSURF()).append("\",");
            sql.append("\"").append(rowData.getL_STDSURF()).append("\",");
            sql.append("\"").append(rowData.getL_STDALIGN()).append("\",");

            sql.append("\"").append(rowData.getR_STDALIGN()).append("\",");
            sql.append("\"").append(rowData.getSTDGAUGE()).append("\",");
            sql.append("\"").append(rowData.getSTDTWIST()).append("\",");
            sql.append("\"").append(rowData.getSTDCROSSLEVEL()).append("\"),");
        }
        String resultSql = sql.toString().substring(0, sql.toString().length() - 1) + ";";
        Connection connection = null;
        try {
            connection = MySqlUtil.getConn0();
            Statement statement = connection.createStatement();
            connection.setAutoCommit(false);
            int num = statement.executeUpdate(resultSql);
            connection.commit();
            statement.close();

            task.log("tqi值插入" + num + "条成功!");
            System.out.println("tqi值插入" + num + "条成功!");
            Log.log.writeLog(0, "tqi值插入" + num + "条成功!");
            sum += num;
            MySqlUtil.returnConn(connection);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            MySqlUtil.rollback(connection);
            Log.log.writeLog(-1, "数据插入异常，已回滚\n" + ExceptionUtil.getExceptionInfo(e));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取连接失败!" + e.getMessage());
        }
        MySqlUtil.returnConn(connection);
        return false;
    }
}

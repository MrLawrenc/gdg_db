package application.table;


import java.math.BigDecimal;

/**
 * @author : LiuMingyao
 * @date : 2019/12/3 15:10
 * @description : TODO
 */
public class TqiPo {
    public String getRecordNumber() {
        return recordNumber;
    }

    @Override
    public String toString() {
        return "TqiPo{" +
                "recordNumber='" + recordNumber + '\'' +
                ", subCode='" + subCode + '\'' +
                ", runDate='" + runDate + '\'' +
                ", runTime='" + runTime + '\'' +
                ", fromPost='" + fromPost + '\'' +
                ", fromMinor='" + fromMinor + '\'' +
                ", tQIMetricName='" + tQIMetricName + '\'' +
                ", tQIValue='" + tQIValue + '\'' +
                ", basePost='" + basePost + '\'' +
                ", trackID='" + trackID + '\'' +
                ", runID='" + runID + '\'' +
                ", lineName='" + lineName + '\'' +
                ", direction='" + direction + '\'' +
                ", powerSectionName='" + powerSectionName + '\'' +
                ", MAXSPEED='" + MAXSPEED + '\'' +
                ", MEANSPEED='" + MEANSPEED + '\'' +
                ", STDLATACCEL='" + STDLATACCEL + '\'' +
                ", STDVERTACCEL='" + STDVERTACCEL + '\'' +
                ", TBCE='" + TBCE + '\'' +
                ", STDSUMS='" + STDSUMS + '\'' +
                ", EXCEEDED='" + EXCEEDED + '\'' +
                ", R_STDSURF='" + R_STDSURF + '\'' +
                ", L_STDSURF='" + L_STDSURF + '\'' +
                ", L_STDALIGN='" + L_STDALIGN + '\'' +
                ", R_STDALIGN='" + R_STDALIGN + '\'' +
                ", STDGAUGE='" + STDGAUGE + '\'' +
                ", STDTWIST='" + STDTWIST + '\'' +
                ", STDCROSSLEVEL='" + STDCROSSLEVEL + '\'' +
                ", kmMark=" + kmMark +
                '}';
    }

    public void setRecordNumber(String recordNumber) {
        this.recordNumber = recordNumber;
    }

    public String getSubCode() {
        return subCode;
    }

    public void setSubCode(String subCode) {
        this.subCode = subCode;
    }

    public String getRunDate() {
        return runDate;
    }

    public void setRunDate(String runDate) {
        this.runDate = runDate;
    }

    public String getRunTime() {
        return runTime;
    }

    public void setRunTime(String runTime) {
        this.runTime = runTime;
    }

    public String getFromPost() {
        return fromPost;
    }

    public void setFromPost(String fromPost) {
        this.fromPost = fromPost;
    }

    public String getFromMinor() {
        return fromMinor;
    }

    public void setFromMinor(String fromMinor) {
        this.fromMinor = fromMinor;
    }

    public String gettQIMetricName() {
        return tQIMetricName;
    }

    public void settQIMetricName(String tQIMetricName) {
        this.tQIMetricName = tQIMetricName;
    }

    public String gettQIValue() {
        return tQIValue;
    }

    public void settQIValue(String tQIValue) {
        this.tQIValue = tQIValue;
    }

    public String getBasePost() {
        return basePost;
    }

    public void setBasePost(String basePost) {
        this.basePost = basePost;
    }

    public String getTrackID() {
        return trackID;
    }

    public void setTrackID(String trackID) {
        this.trackID = trackID;
    }

    public String getRunID() {
        return runID;
    }

    public void setRunID(String runID) {
        this.runID = runID;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getPowerSectionName() {
        return powerSectionName;
    }

    public void setPowerSectionName(String powerSectionName) {
        this.powerSectionName = powerSectionName;
    }

    public String getMAXSPEED() {
        return MAXSPEED;
    }

    public void setMAXSPEED(String MAXSPEED) {
        this.MAXSPEED = MAXSPEED;
    }

    public String getMEANSPEED() {
        return MEANSPEED;
    }

    public void setMEANSPEED(String MEANSPEED) {
        this.MEANSPEED = MEANSPEED;
    }

    public String getSTDLATACCEL() {
        return STDLATACCEL;
    }

    public void setSTDLATACCEL(String STDLATACCEL) {
        this.STDLATACCEL = STDLATACCEL;
    }

    public String getSTDVERTACCEL() {
        return STDVERTACCEL;
    }

    public void setSTDVERTACCEL(String STDVERTACCEL) {
        this.STDVERTACCEL = STDVERTACCEL;
    }

    public String getTBCE() {
        return TBCE;
    }

    public void setTBCE(String TBCE) {
        this.TBCE = TBCE;
    }

    public String getSTDSUMS() {
        return STDSUMS;
    }

    public void setSTDSUMS(String STDSUMS) {
        this.STDSUMS = STDSUMS;
    }

    public String getEXCEEDED() {
        return EXCEEDED;
    }

    public void setEXCEEDED(String EXCEEDED) {
        this.EXCEEDED = EXCEEDED;
    }

    public TqiPo(String recordNumber, String subCode, String runDate, String runTime, String fromPost, String fromMinor,
                 String tQIMetricName, String tQIValue, String basePost, String trackID, String runID, String lineName,
                 String direction, String powerSectionName) {
        this.recordNumber = recordNumber;
        this.subCode = subCode;
        this.runDate = runDate;
        this.runTime = runTime;
        this.fromPost = fromPost;
        this.fromMinor = fromMinor;
        this.tQIMetricName = tQIMetricName;
        this.tQIValue = tQIValue;
        this.basePost = basePost;
        this.trackID = trackID;
        this.runID = runID;
        this.lineName = lineName;
        this.direction = direction;
        this.powerSectionName = powerSectionName;
    }

    private String recordNumber;
    private String subCode;
    private String runDate;
    private String runTime;
    private String fromPost;
    private String fromMinor;
    private String tQIMetricName;
    private String tQIValue;
    private String basePost;
    private String trackID;
    private String runID;
    private String lineName;
    private String direction;
    private String powerSectionName;

    /**
     * 行转为列的字段
     */
    private String MAXSPEED;
    private String MEANSPEED;
    private String STDLATACCEL;
    private String STDVERTACCEL;
    private String TBCE;

    private String STDSUMS;
    private String EXCEEDED;
    private String R_STDSURF;
    private String L_STDSURF;
    private String L_STDALIGN;

    private String R_STDALIGN;

    public String getR_STDSURF() {
        return R_STDSURF;
    }

    public void setR_STDSURF(String r_STDSURF) {
        R_STDSURF = r_STDSURF;
    }

    public String getL_STDSURF() {
        return L_STDSURF;
    }

    public void setL_STDSURF(String l_STDSURF) {
        L_STDSURF = l_STDSURF;
    }

    public String getL_STDALIGN() {
        return L_STDALIGN;
    }

    public void setL_STDALIGN(String l_STDALIGN) {
        L_STDALIGN = l_STDALIGN;
    }

    public String getR_STDALIGN() {
        return R_STDALIGN;
    }

    public void setR_STDALIGN(String r_STDALIGN) {
        R_STDALIGN = r_STDALIGN;
    }

    public String getSTDGAUGE() {
        return STDGAUGE;
    }

    public void setSTDGAUGE(String STDGAUGE) {
        this.STDGAUGE = STDGAUGE;
    }

    public String getSTDTWIST() {
        return STDTWIST;
    }

    public void setSTDTWIST(String STDTWIST) {
        this.STDTWIST = STDTWIST;
    }

    public String getSTDCROSSLEVEL() {
        return STDCROSSLEVEL;
    }

    public void setSTDCROSSLEVEL(String STDCROSSLEVEL) {
        this.STDCROSSLEVEL = STDCROSSLEVEL;
    }

    private String STDGAUGE;
    private String STDTWIST;
    private String STDCROSSLEVEL;


    /**
     * 设置转为列之后的值
     */
    public void setTargetFields(String MAXSPEED, String MEANSPEED, String STDLATACCEL, String STDVERTACCEL,
                                String TBCE, String STDSUMS, String EXCEEDED, String R_STDSURF,
                                String L_STDSURF, String L_STDALIGN, String R_STDALIGN, String STDGAUGE,
                                String STDTWIST, String STDCROSSLEVEL) {
        this.MAXSPEED = MAXSPEED;
        this.MEANSPEED = MEANSPEED;
        this.STDLATACCEL = STDLATACCEL;
        this.STDVERTACCEL = STDVERTACCEL;

        this.TBCE = TBCE;
        this.STDSUMS = STDSUMS;
        this.EXCEEDED = EXCEEDED;
        this.R_STDSURF = R_STDSURF;

        this.L_STDSURF = L_STDSURF;
        this.L_STDALIGN = L_STDALIGN;
        this.R_STDALIGN = R_STDALIGN;
        this.STDGAUGE = STDGAUGE;

        this.STDTWIST = STDTWIST;
        this.STDCROSSLEVEL = STDCROSSLEVEL;
    }

    private BigDecimal kmMark;

    public void setKmMark(BigDecimal kmMark) {
        this.kmMark = kmMark;
    }
}
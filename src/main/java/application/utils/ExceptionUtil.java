package application.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author : LiuMingyao
 * @date : 2019/8/14 17:23
 * @description : 异常工具类
 */
public class ExceptionUtil {
    /**
     * @author : LiuMing
     * @date : 2019/8/14 17:27
     * @description :   获取完整的堆栈字符串信息
     */
    public static String getExceptionInfo(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter, true));
        return stringWriter.getBuffer().toString();
    }


    public static String appendExceptionInfo(Throwable t) {
        String prefix = "\t异常信息如下:\n";
        StringWriter stringWriter = new StringWriter();
        t.printStackTrace(new PrintWriter(stringWriter, true));
        return prefix + stringWriter.getBuffer().toString();
    }
}
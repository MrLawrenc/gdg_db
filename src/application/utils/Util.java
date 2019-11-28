package application.utils;

import java.io.File;
import java.util.Set;

import application.controller.MyController;
import application.db.MySqlUtil;

public class Util {
	public static String[] tables = new String[]{"tvalue", "tqi", "kms", "defects", "curves"};

	/**
	 * 根据文件名获取其他信息 <br>
	 * 吕临线_太原南_(35.950-3.848) <br>
	 * 京包线上行_大同_(371-236.5)
	 */
	public static String[] getInfo(String dbFullName) {
		Log log = Log.log;
		// 线路
		String lineName;
		// 行别
		String direction;
		// 工务段
		String powerSection;
		try {
			String[] split = dbFullName.split("_");
			if (split[0].contains("行")) {
				lineName = split[0].substring(0, split[0].length() - 2);
				direction = split[0].substring(split[0].length() - 2);
			} else {
				lineName = split[0];
				direction = "0";
			}
			powerSection = split[1] + "工务段";
		} catch (Exception e) {
			log.writeLog(-1, "异常:" + dbFullName + ExceptionUtil.appendExceptionInfo(e));
			return new String[]{"文件名异常", "文件名异常", "文件名异常"};
		}
		return new String[]{lineName, direction, powerSection};
	}

	public static void exit() {
		Log.log.writeLog(0, "退出，关闭资源.......................");
		Log.log.close();
		MySqlUtil.closeConnection();
		System.exit(0);
	}

	/**
	 * 获取所有线路所在的目录
	 */
	public static void searchFile(File parentFile, Set<File> result) {
		File[] files = parentFile.listFiles();
		if (files == null) {
			return;
		}
		for (File currentFile : files) {
			if (currentFile.isDirectory()) {
				if ((currentFile.getName().contains("上行")
						|| currentFile.getName().contains("下行"))) {
					result.add(currentFile.getParentFile());
					return;
				} else {
					searchFile(currentFile, result);
				}
			}

		}
	}

}

package application.utils;

import application.controller.MyController.Cache;
import application.db.AccessUtil;
import application.table.Defects;
import application.table.Kms;
import application.table.TValueUtil;
import application.table.TqiUtil;
import javafx.concurrent.Task;

import java.io.File;
import java.util.*;

/**
 * 批量入库异步线程类
 * 
 * @author mrliu
 */
public class MyTask extends Task<String> {
	/**
	 * 数据入库模式选择，默认单个数据库入一次库
	 */
	private boolean useBatch = false;

	private Cache cache;

	public MyTask(File parentFile, Cache cache) {
		this.parentFile = parentFile;
		this.cache = cache;
	}

	private File parentFile;
	/**
	 * 每张表的表头和所有行记录的 key前缀
	 */
	private String tableNamePre = "name:";
	private String tableDataPre = "data:";

	/**
	 * 暴露给外部更新日志的方法
	 */
	public void log(String message) {
		updateMessage(message);
	}

	/**
	 * 这儿jfx有一个bug，当消息冲刷太快，会导致task的监听，即task.messageProperty().addListener收到的消息不完整，前面的消息会被冲刷掉，
	 * 因此这儿临时解决，每个消息到达，睡眠10ms.加入线程池队列可以不阻塞updateMessage方法的调用方
	 */
	@Override
	protected void updateMessage(String message) {
		try {
			ThreadUtil.BLOCK_QUEUE_EXECUTOR.execute(() -> {
				// System.out.println("updateMessage:"+message);
				super.updateMessage(message);
				try {
					Thread.sleep(2);
				} catch (Exception e) {

				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
		}
	}

	/**
	 * 异步方法，类似于Run()
	 */
	@Override
	protected String call() throws Exception {
		List<File> allDbFiles = AccessUtil.accordRequireFiles(parentFile);
		if (allDbFiles.size() == 0) {
			return "0该文件夹下没数据！";
		}
		delDoneFile(allDbFiles);
		if (allDbFiles.size() == 0) {
			return "1文件夹下数据都已入过库！";
		}

		Log.log.writeLog(0, "db数量：" + allDbFiles.size());
		updateProgress(3, 100);
		if (useBatch) {
			batchDbAdd(allDbFiles);
		} else {
			double size = allDbFiles.size();
			double temp = Util.round(3, 5);
			double everyNum = Util.round(97.00000 / Util.round(size, 5), 5);
			for (File file : allDbFiles) {

				singleDbAdd(temp, everyNum, file);
				temp += everyNum;
				updateProgress(temp, 100);
			}
			System.out.println("总共插入数据:tqi->" + TqiUtil.sum + "  tvalue->" + TValueUtil.sum + "  defects->"
					+ Defects.sum + "  kms->" + Kms.sum);
		}
		updateProgress(100, 100);
		//执行更新操作
		executeSqlUpdate();
		return "总共插入数据:tqi->" + TqiUtil.sum + "  tvalue->" + TValueUtil.sum + "  defects->" + Defects.sum + "  kms->"
				+ Kms.sum + "\n";
	}

	/**
	 * 批量access数据库处理更新
	 */
	public void batchDbAdd(List<File> allDbFiles) {
		try {
			int limit = 2;
			int dbNum = allDbFiles.size();
			String aaString = "源数据库数量:" + dbNum;
			updateMessage(aaString);

			if (dbNum > limit) {
				int num = dbNum / limit;
				for (int i = 0; i < num; i++) {
					updateMessage("处理 " + (i * limit) + " 到 " + (i + 1) * limit);
					batchAdd0(allDbFiles.subList(i * limit, (i + 1) * limit));
				}
				if (num * limit != dbNum) {
					updateMessage("处理 " + (num * limit) + " 到 " + dbNum);
					batchAdd0(allDbFiles.subList(num * limit, dbNum));
				}
			}
			updateMessage("数据插入成功......................");
			executeSqlUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			Log.log.writeLog(-1, "数据插入失败！" + ExceptionUtil.appendExceptionInfo(e));
		}
	}

	/**
	 * @param start 进度条开始位置
	 * @param step  进度条在该方法内能使用的数量
	 * @description 单个access数据库处理，主要是方便得知进度条，否则应使用批量插入，节约时间
	 */
	public void singleDbAdd(double start, double step, File dbFile) {
		try {
			Map<String, List<List<String>>> allDataMap = new HashMap<String, List<List<String>>>(1000);
			// ArrayList<File> allFiles = AccessUtil.accordRequireFiles(tempParentFile);
			Log.log.writeLog(1, "正在获取数据库文件" + dbFile.getName() + "的所有数据");
			String dbPath = dbFile.getAbsolutePath();
			List<String> allTableName = AccessUtil.allTableName(dbPath);
			for (String tableName : allTableName) {
				// logUtil.infoLog("tableName:" + tableName);
				String tableNameKey = tableNamePre + tableName;
				String tableDataKey = tableDataPre + tableName;
				List<List<String>> tableData = AccessUtil.getTableDataByTableName(dbPath, tableName);
				List<List<String>> metaList = new ArrayList<List<String>>(1);
				metaList.add(tableData.get(0));
				if (allDataMap.get(tableNameKey) == null) {
					allDataMap.put(tableNameKey, metaList);
				}
				// 删除表头信息
				tableData.remove(0);
				System.out.println(tableName + "  表数据  " + tableData.size());
				if (allDataMap.get(tableDataKey) == null) {
					allDataMap.put(tableDataKey, tableData);
				} else {
					allDataMap.get(tableDataKey).addAll(tableData);
				}
			}
			// logUtil.detailLog("关闭数据库连接: " + dbPath);
			AccessUtil.closeConn(dbPath);

			addOtherInfo4Single(dbFile, allDataMap);

			dealAllData(dbFile.getAbsolutePath(), allDataMap);
		} catch (Exception e) {
			Log.log.writeLog(-1, "0异常，程序即将关闭" + ExceptionUtil.appendExceptionInfo(e));
		}

	}

	/**
	 * 对所有的db进行数据获取，并插入mysql
	 * 
	 * @param allFiles 所有的db文件
	 */
	public void batchAdd0(List<File> allFiles) {
		try {
			Map<String, List<List<String>>> allDataMap = new HashMap<String, List<List<String>>>(1000);
			// ArrayList<File> allFiles = AccessUtil.accordRequireFiles(tempParentFile);
			for (File dbFile : allFiles) {
				Log.log.writeLog(1, "正在获取数据库文件" + dbFile.getName() + "的所有数据");

				String dbPath = dbFile.getAbsolutePath();
				List<String> allTableName = AccessUtil.allTableName(dbPath);
				for (String tableName : allTableName) {
					// logUtil.infoLog("tableName:" + tableName);
					String tableNameKey = tableNamePre + tableName;
					String tableDataKey = tableDataPre + tableName;
					List<List<String>> tableData = AccessUtil.getTableDataByTableName(dbPath, tableName);
					List<List<String>> metaList = new ArrayList<List<String>>(1);
					metaList.add(tableData.get(0));
					if (allDataMap.get(tableNameKey) == null) {
						allDataMap.put(tableNameKey, metaList);
					}
					// 删除表头信息
					tableData.remove(0);
					if (allDataMap.get(tableDataKey) == null) {
						allDataMap.put(tableDataKey, tableData);
					} else {
						allDataMap.get(tableDataKey).addAll(tableData);
					}
				}
				// logUtil.detailLog("关闭数据库连接: " + dbPath);
				AccessUtil.closeConn(dbPath);
			}
			addOtherInfo(allFiles, allDataMap);

			// dealAllData(allDataMap);

		} catch (Exception e) {
			Log.log.writeLog(-1, "异常，程序即将关闭" + ExceptionUtil.appendExceptionInfo(e));
		}

	}

	/**
	 * 数据插入完毕之后执行更新脚本
	 */
	private void executeSqlUpdate() {
		Log.log.writeLog(0, "开始执行sql更新脚本!");

		Log.log.writeLog(0, "执行sql更新脚本成功!");
	}

	/**
	 * 添加线路、行别等信息
	 */
	private void addOtherInfo(List<File> allFiles, Map<String, List<List<String>>> allDataMap) {
		for (File file : allFiles) {
			String[] info = Util.getInfo(file.getName());
			List<List<String>> tvalue = allDataMap.get(tableDataPre + Util.tables[0]);
			add0(tvalue, info);
			List<List<String>> tqi = allDataMap.get(tableDataPre + Util.tables[1]);
			add0(tqi, info);
			List<List<String>> kms = allDataMap.get(tableDataPre + Util.tables[2]);
			add0(kms, info);
			List<List<String>> defects = allDataMap.get(tableDataPre + Util.tables[3]);
			add0(defects, info);
		}

	}

	private void addOtherInfo4Single(File file, Map<String, List<List<String>>> allDataMap) {
		String[] info = Util.getInfo(file.getName());
		// System.out.println(file.getName()+" "+info[0]+" "+info[1]+" "+info[2]);
		List<List<String>> tvalue = allDataMap.get(tableDataPre + Util.tables[0]);
		add0(tvalue, info);
		List<List<String>> tqi = allDataMap.get(tableDataPre + Util.tables[1]);
		add0(tqi, info);
		List<List<String>> kms = allDataMap.get(tableDataPre + Util.tables[2]);
		add0(kms, info);
		List<List<String>> defects = allDataMap.get(tableDataPre + Util.tables[3]);
		add0(defects, info);

	}

	private void add0(List<List<String>> data, String[] info) {
		data.forEach(list -> {
			list.add(info[0]);
			list.add(info[1]);
			list.add(info[2]);
		});
	}

	/**
	 * 保存所有数据到mysql
	 * 
	 * @param allData
	 * @throws InterruptedException
	 */
	public void dealAllData(String dbPath, Map<String, List<List<String>>> allData) throws InterruptedException {
		// 脏数据清理 TODO
//		List<String> tableStrings = new ArrayList<String>();
//		for (String unFinishFileInfo : cache.unFinishFilePathList) {
//			String[] split = unFinishFileInfo.split(" ");
//			if (split[0].equals(dbPath)) {
//				tableStrings.add(split[1]);
//			}
//		}

		// logUtil.infoLog(mysqlUrl.getText());
		RecoredInfo.recored.recoredFileInfo(false, dbPath + " tvalue");
		boolean tvalueSuccess = TValueUtil.save(allData.get(tableNamePre + Util.tables[0]),
				allData.get(tableDataPre + Util.tables[0]), this);
		if (tvalueSuccess) {
			RecoredInfo.recored.recoredFileInfo(true, dbPath + " tvalue");
		}
		allData.remove(tableDataPre + Util.tables[0]);

		RecoredInfo.recored.recoredFileInfo(false, dbPath + " tqi");
		boolean tqiSuccess = TqiUtil.save(allData.get(tableNamePre + Util.tables[1]),
				allData.get(tableDataPre + Util.tables[1]), this);
		if (tqiSuccess) {
			RecoredInfo.recored.recoredFileInfo(true, dbPath + " tqi");
		}
		allData.remove(tableDataPre + Util.tables[1]);

		RecoredInfo.recored.recoredFileInfo(false, dbPath + " kms");
		boolean kmsSuccess = Kms.save(allData.get(tableNamePre + Util.tables[2]),
				allData.get(tableDataPre + Util.tables[2]), this);
		allData.remove(tableDataPre + Util.tables[2]);
		if (kmsSuccess) {
			RecoredInfo.recored.recoredFileInfo(true, dbPath + " kms");
		}
		// alarm表
		RecoredInfo.recored.recoredFileInfo(false, dbPath + " defects");
		boolean DefectsSuccess = Defects.save(allData.get(tableNamePre + Util.tables[3]),
				allData.get(tableDataPre + Util.tables[3]), this);
		if (DefectsSuccess) {
			RecoredInfo.recored.recoredFileInfo(true, dbPath + " defects");
		}
		allData.remove(tableDataPre + Util.tables[3]);

	}

	/**
	 * 剔除本次扫描到文件夹（删除之前入过库的重复db文件）
	 */
	private void delDoneFile(List<File> resultFile) {
		for (String finishPath : cache.doneFilePathList) {
			Iterator<File> it = resultFile.iterator();
			while (it.hasNext()) {
				File value = it.next();
				if (finishPath.contains(value.getAbsolutePath())) {
					it.remove();
				}
			}
		}
	}
}
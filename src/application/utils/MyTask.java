package application.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import application.db.AccessUtil;
import application.table.Defects;
import javafx.concurrent.Task;

/**
 * 批量入库异步线程类
 * 
 * @author mrliu
 */
public class MyTask extends Task<String> {
	/**
	 * 数据入库模式选择，默认单个数据库入一次库
	 */
	private boolean useBatch = true;

	public MyTask(String parentPath) {
		this.parentPath = parentPath;
	}

	private String parentPath;
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
		CompletableFuture.runAsync(() -> {
			super.updateMessage(message);
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}, Log.BLOCK_QUEUE_EXECUTOR);
	}

	/**
	 * 异步方法，类似于Run()
	 */
	@Override
	protected String call() throws Exception {
		List<File> allDbFiles = AccessUtil.accordRequireFiles(new File(parentPath));
		updateProgress(2, 100);
		if (useBatch) {
			batchDbAdd(allDbFiles);
		} else {
			double size = allDbFiles.size();
			int temp = 2;
			double everyNum = 98.000 / size;
			for (File file : allDbFiles) {
				try {
					Thread.sleep(1000);
					temp += everyNum;
					updateProgress(temp, 100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		updateProgress(100, 100);
		return "hello";
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
				if (num*limit!=dbNum) {
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
	 * 单个access数据库处理，主要是方便得知进度条，否则应使用批量插入，节约时间
	 */
	public void singleDbAdd(File dbFile) {
		try {
			Map<String, List<List<String>>> allDataMap = new HashMap<String, List<List<String>>>(
					1000);
			// ArrayList<File> allFiles = AccessUtil.accordRequireFiles(tempParentFile);
			Log.log.writeLog(1, "正在获取数据库文件" + dbFile.getName() + "的所有数据");
			String dbPath = dbFile.getAbsolutePath();
			List<String> allTableName = AccessUtil.allTableName(dbPath);
			for (String tableName : allTableName) {
				// logUtil.infoLog("tableName:" + tableName);
				String tableNameKey = tableNamePre + tableName;
				String tableDataKey = tableDataPre + tableName;
				List<List<String>> tableData = AccessUtil.getTableDataByTableName(dbPath,
						tableName);
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

			addOtherInfo4Single(dbFile, allDataMap);

			dealAllData(allDataMap);

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
			Map<String, List<List<String>>> allDataMap = new HashMap<String, List<List<String>>>(
					1000);
			// ArrayList<File> allFiles = AccessUtil.accordRequireFiles(tempParentFile);
			for (File dbFile : allFiles) {
				Log.log.writeLog(1, "正在获取数据库文件" + dbFile.getName() + "的所有数据");

				String dbPath = dbFile.getAbsolutePath();
				List<String> allTableName = AccessUtil.allTableName(dbPath);
				for (String tableName : allTableName) {
					// logUtil.infoLog("tableName:" + tableName);
					String tableNameKey = tableNamePre + tableName;
					String tableDataKey = tableDataPre + tableName;
					List<List<String>> tableData = AccessUtil.getTableDataByTableName(dbPath,
							tableName);
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

			dealAllData(allDataMap);

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
	public void dealAllData(Map<String, List<List<String>>> allData) throws InterruptedException {

		// logUtil.infoLog(mysqlUrl.getText());
		// TValueUtil.save(allData.get(tableNamePre + Util.tables[0]), allData.get(tableDataPre + Util.tables[0]), this);
		// allData.remove(tableDataPre + Util.tables[0]);
		//
		// TqiUtil.save0(allData.get(tableNamePre + Util.tables[1]), allData.get(tableDataPre + Util.tables[1]), this);
		// allData.remove(tableDataPre + Util.tables[1]);
		//
		// Kms.save(allData.get(tableNamePre + Util.tables[2]), allData.get(tableDataPre + Util.tables[2]), this);
		// allData.remove(tableDataPre + Util.tables[2]);
		// alarm表
		Defects.save(allData.get(tableNamePre + Util.tables[3]),
				allData.get(tableDataPre + Util.tables[3]), this);
		allData.remove(tableDataPre + Util.tables[3]);

	}
}
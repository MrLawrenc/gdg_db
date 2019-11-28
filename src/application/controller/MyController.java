package application.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import application.db.AccessUtil;
import application.db.MySqlUtil;
import application.table.Kms;
import application.table.TValueUtil;
import application.table.TqiUtil;
import application.utils.ExceptionUtil;
import application.utils.Log;
import application.utils.MyTask;
import application.utils.RecoredInfo;
import application.utils.Util;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogEvent;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

@SuppressWarnings("all")
public class MyController<E> implements Initializable {
	@FXML
	private Tab oneTab;
	@FXML
	private TextField parentFilePath;
	@FXML
	private TextField mysqlUrl;
	@FXML
	private TextField mysqlUsername;
	@FXML
	private TextField mysqlPwd;
	@FXML
	private ComboBox<String> sourceDbType;
	@FXML
	private ComboBox<String> targetDbType;
	@FXML
	private ComboBox<String> sourceDb;
	@FXML
	private ComboBox<String> targetDb;
	@FXML
	private ComboBox<String> sourceTable;
	@FXML
	private ComboBox<String> targetTable;
	@FXML
	private ComboBox<String> modle;
	@FXML
	private ComboBox<String> selectMap;
	/**
	 * 字段对应关系的模式选择
	 */
	@FXML
	private ComboBox<String> fieldMap;

	@FXML
	private Button dbBtn;
	@FXML
	private ListView<String> sourceFields;
	@FXML
	private ListView<String> targetFields;
	@FXML
	private TextArea specialMap;
	@FXML
	private TextArea log;
	@FXML
	private ProgressBar progressBar;
	/**
	 * 弹窗控件
	 */
	private Dialog<ButtonType> dialog;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		dialog = new Dialog<ButtonType>();
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
		dialog.setTitle("温馨提示");
		dialog.setContentText("nei rong");
		Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
		dialog.setOnCloseRequest(new EventHandler<DialogEvent>() {
			@Override
			public void handle(DialogEvent event) {
				System.out.println("关闭提示框");
			}
		});

		/**
		 * fixme 固定死数据库类型选择
		 */
		sourceDbType.getSelectionModel().select("Access");
		sourceDbType.setDisable(true);
		targetDbType.getSelectionModel().select("Mysql");
		targetDbType.setDisable(true);
		parentFilePath
				.setText("E:\\\\工电供资料\\\\document\\\\6.客户资料\\\\工务\\\\工务检测数据\\\\2019年第三季度综合检测车联检");
		// parentFilePath.setText("F:\\gtdq\\资料");

		// mysqlUrl.setText(
		// "jdbc:mysql://localhost:3306/study?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
		mysqlUrl.setText(
				"jdbc:mysql://47.96.158.220:3306/study?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC");
		// mysqlUrl.setText(
		// "jdbc:mysql://192.168.3.52:3306/idcty?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC");
		mysqlUsername.setText("root");
		// mysqlPwd.setText("admin");
		mysqlPwd.setText("@123lmyLMY.");
		// mysqlUsername.setText("idcty");
		// mysqlPwd.setText("123456Aa");
	}

	/*
	 * =================================分割线=========================================
	 */

	/**
	 * 按照定死的规则，向mysql批量插入所有的access数据
	 */
	public void batchAdd() {
		try {
			initFileInfo();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (cache.url != null) {
			Log.log.writeLog(0, "获取到数据库连接信息缓存，直接使用！");
			mysqlUrl.setText(cache.url);
			mysqlUsername.setText(cache.username);
			mysqlPwd.setText(cache.password);
		}

		String url = mysqlUrl.getText();
		String username = mysqlUsername.getText();
		String password = mysqlPwd.getText();
		if (url.trim().equals("") || username.trim().equals("") || password.trim().equals("")) {
			dialog.setContentText("MySql数据库连接信息设置不正确!");
			dialog.show();
			return;
		}
		RecoredInfo.recored.recoredConnInfo(url, username, password);

		log.clear();

		String path = parentFilePath.getText();
		System.out.println("path:" + path);
		if (path.trim().equals("")) {
			Log.log.writeLog(-1, "access数据库文件夹设置不正确!");
			dialog.setContentText("文件夹不能为空!");
			dialog.show();
			return;
		}

		Thread initConn = new Thread(() -> MySqlUtil.initConn(mysqlUrl.getText(),
				mysqlUsername.getText(), mysqlPwd.getText()));
		initConn.start();
		progressBar.setProgress(0.1);
		File parentFile;
		try {
			parentFile = new File(path);
			if (!parentFile.exists()) {
				dialog.setContentText("文件夹" + path + "不存在!");
				dialog.show();
			}
		} catch (Exception e) {
			dialog.setContentText("打开文件夹" + path + "失败!");
			dialog.show();
			return;
		}
		MyTask task = new MyTask(parentFile);
		// 监听task的updateMessage方法,实现日志更新
		task.messageProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue,
					String newValue) {
				log.appendText(LocalDateTime.now() + " " + newValue + "\n");
			}
		});
		// 监听进度，实现进度条更新
		task.progressProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue,
					Number newValue) {
				System.out.println("进度条值:" + newValue.doubleValue());
				progressBar.setProgress(newValue.doubleValue());

			}
		});

		// 获取监听task的call方法返回值
		task.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue,
					String newValue) {
				// logUtil.infoLog("全部执行完毕");
				System.out.println(Thread.currentThread().getName() + " call() 返回值：" + newValue);
				log.appendText(LocalDateTime.now() + " 数据处理完毕!\n");
				Log.log.writeLog(0, "数据处理完毕!");
			}
		});
		new Thread(task, "gtdq-gdt-task").start();
	}

	private Cache cache = new Cache();

	public boolean initFileInfo() throws IOException {
		File file = new File(RecoredInfo.recored.recoredFileName);
		if (!file.exists()) {
			return false;
		}
		FileInputStream fis = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		String line = "";

		while ((line = br.readLine()) != null) {

			// 添加换行符
			// tempStream.append(System.getProperty("line.separator"))
			if (line.contains(RecoredInfo.recored.mysqlInfo)) {
				String[] mysqlInfo = line.split(RecoredInfo.recored.mysqlInfo)[1].split(" ");
				cache.url = mysqlInfo[0];
				cache.username = mysqlInfo[1];
				cache.password = mysqlInfo[2];
			}
			if (line.contains(RecoredInfo.recored.filePth)) {

			}
		}
		br.close();
		isr.close();
		fis.close();
		return true;
	}

	private static class Cache {
		private String url;
		private String username;
		private String password;
		private List<String> doneFilePathList;
	}
}

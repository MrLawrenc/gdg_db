package application.controller;

import application.db.MySqlUtil;
import application.utils.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("all")
public class MyController implements Initializable {
    @FXML
    private TextField parentFilePath;
    @FXML
    private Button batch;
    @FXML
    private TextArea log;
    @FXML
    private ProgressBar progressBar;


    /**
     * 选中的文件夹
     */
    private File selectFile;
    /**
     * 弹窗控件
     */
    private Dialog<ButtonType> dialog;


    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * 是否是正在运行状态
     */
    private AtomicBoolean running = new AtomicBoolean(false);
    /**
     * 获取所有已入库的记录
     */
    private String getRecordInfoSql = "select db_file_name,table_name from data_store_record where parent_file_name=? and state=1";
    /**
     * 存储当前选中目录parentFile下面所有保存成功的数据,初始化容量根据给的资料确定(一个季度70个库，每个库5张表，大概是350条记录，其余做冗余)
     */
    private List<String> done = Collections.synchronizedList(new ArrayList<>(500));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        batch.setDisable(true);
        initDialog();
        //parentFilePath.setText("E:\\\\工电供资料\\\\document\\\\6.客户资料\\\\工务\\\\工务检测数据\\\\2019年第三季度综合检测车联检");
        parentFilePath.setDisable(true);
        Platform.runLater(() -> {
            ConnTask connTask = new ConnTask();
            connTask.messageProperty().addListener((observableValue, oldValue, newValue) -> {
                batch.setDisable(true);
                dialog.setContentText(newValue);
                dialog.show();
            });
            ThreadUtil.BLOCK_QUEUE_EXECUTOR.execute(connTask);
        });
    }

    /*
     * =================================分割线=========================================
     */


    private void initDialog() {
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
    }


    /**
     * 获取用户选择的文件夹
     */
    public void selectFile() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File file = directoryChooser.showDialog(stage);
        if (file == null) {
            dialog.setContentText("选择文件夹为空，请重新选择!");
            dialog.show();
            return;
        }
        String path = file.getPath();
        if ("/".equals(path.split(":")[1]) || "\\".equals(path.split(":")[1])) {
            dialog.setContentText("不能选择最上层目录，请重新选择!");
            dialog.show();
            return;
        }
        String[] abstractInfo = Util.getAbstractInfo(file.getName());
        if ("".equals(abstractInfo[0])) {
            dialog.setContentText("文件名不符合规范，批次信息将无法导入!");
            dialog.show();
            return;
        }
        selectFile = file;
        CompletableFuture.runAsync(() -> initFileInfo(), ThreadUtil.BLOCK_QUEUE_EXECUTOR);
        batch.setDisable(false);
        parentFilePath.setText(path);
    }

    /**
     * 按照定死的规则，向mysql批量插入所有的access数据
     */
    public void batchAdd() {
        progressBar.setProgress(0);
        if (running.get()) {
            dialog.setContentText("程序正在运行，请勿重复执行！");
            dialog.show();
            return;
        }
        //RecordedInfo.recored.recoredConnInfo(url, username, password);
        progressBar.setProgress(0.01);
        log.clear();


        running.compareAndSet(false, true);
        progressBar.setProgress(0.01);
        MyTask task = new MyTask(selectFile, done);
        // 监听task的updateMessage方法,实现日志更新
        task.messageProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.contains("&")) {
                    dialog.setContentText(newValue);
                    dialog.show();
                } else {
                    log.appendText(LocalDateTime.now() + " " + newValue + "\n");
                }
            }
        });
        // 监听进度，实现进度条更新
        task.progressProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                // System.out.println("进度条值:" + newValue.doubleValue());
                progressBar.setProgress(newValue.doubleValue());

            }
        });

        // 获取监听task的call方法返回值
        task.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                running.compareAndSet(true, false);
                if ("0".equals(newValue.subSequence(0, 1))) {
                    log.appendText("\n" + newValue + LocalDateTime.now() + " 数据处理失败!");
                    dialog.setContentText(newValue.substring(1));
                    dialog.show();
                    return;
                }
                log.appendText(newValue + LocalDateTime.now() + " 数据处理完毕!\n");
                Log.log.writeLog(0, "数据处理完毕!\n");
                //解析错误记录
                if (Util.exceptionName.size() > 0) {
                    dialog.setContentText("部分数据入库线路、行别、工务段解析失败，详情查看日志！");
                    dialog.show();
                    String exceptionInfo = Util.exceptionName.stream().reduce((v1, v2) -> v1 + "\n" + v2 + "\n").get();
                    log.appendText("线路、行别、工务段解析失败记录（使用默认值”文件名异常“保存）:\n" + exceptionInfo);
                }
                //未入库信息统计
                if (task.notSave.size() > 0) {
                    String result = "";
                    for (String notSave : task.notSave) {
                        result += "\t"+notSave + "\n";
                    }
                    log.appendText("本次未入库信息如下:\n" + result);
                }
            }
        });
        new Thread(task, "gtdq-gdt-task").start();
        progressBar.setProgress(0.02);

    }


    /**
     * 根据本地文件记录，获取连接信息、以入库的信息
     */
    public void initFileInfo() {
        try {
            Connection conn = MySqlUtil.getConn0();
            PreparedStatement statement = conn.prepareStatement(getRecordInfoSql);
            statement.setString(1, selectFile.getName());
            ResultSet resultSet = statement.executeQuery();
            conn.commit();
            while (resultSet.next()) {
                done.add(resultSet.getString("db_file_name") + resultSet.getString("table_name"));
            }
            resultSet.close();
            statement.close();
            MySqlUtil.returnConn(conn);
            System.out.println("已入库信息获取成功,已入库表数量:" + done.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步连接池初始化
     */
    private static class ConnTask extends Task<Boolean> {

        @Override
        protected Boolean call() throws Exception {

            File file = new File("db.gtdq");
            try (RandomAccessFile dbFile = new RandomAccessFile(file, "r")) {
                String dbUrl = dbFile.readLine().split("url=")[1];
                String dbUsername = dbFile.readLine().split("username=")[1];
                String dbPassword = dbFile.readLine().split("password=")[1];
                // MySqlUtil.initConn(dbUrl.trim(), dbUsername.trim(), dbPassword.trim());
                MySqlUtil.init(dbUrl.trim(), dbUsername.trim(), dbPassword.trim());
            } catch (Exception e) {
                updateMessage("连接到mysql数据库失败,请退出程序重新配置!");
                return false;
            }
            return true;
        }

    }
}

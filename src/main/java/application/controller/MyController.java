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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("all")
public class MyController implements Initializable {
    @FXML
    private Tab oneTab;
    @FXML
    private TextField parentFilePath;
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
    private Button batch;
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
    private String url;
    private String username;
    private String password;
    /**
     * 是否是正在运行状态
     */
    private AtomicBoolean running = new AtomicBoolean(false);

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
        parentFilePath.setText("E:\\\\工电供资料\\\\document\\\\6.客户资料\\\\工务\\\\工务检测数据\\\\2019年第三季度综合检测车联检");
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
        // 初始化文件保存的信息
        try {
            initFileInfo();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
//         mysqlUrl.setText(
//         "jdbc:mysql://192.168.3.52:3306/idcty?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC");
//         mysqlUsername.setText("idcty");
//         mysqlPwd.setText("123456Aa");

    }

    /*
     * =================================分割线=========================================
     */

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
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

        String path = parentFilePath.getText();
        if (path == null || "".equals(path.trim())) {
            dialog.setContentText("请选择数据库文件所在的文件夹目录!");
            dialog.show();
            return;
        }


        running.compareAndSet(false, true);
        progressBar.setProgress(0.01);
        MyTask task = new MyTask(new File(path), cache);
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
                } else if ("1".equals(newValue.subSequence(0, 1))) {
                    dialog.setContentText(newValue.substring(1));
                    dialog.show();
                    return;
                }
                log.appendText(newValue + LocalDateTime.now() + " 数据处理完毕!\n");
                Log.log.writeLog(0, "数据处理完毕!");
                if (Util.exceptionName.size() > 0) {
                    dialog.setContentText("部分数据入库线路、行别、工务段解析失败，详情查看日志！");
                    dialog.show();
                    String exceptionInfo = Util.exceptionName.stream().reduce((v1, v2) -> v1 + v2 + "\n").get();
                    log.appendText("线路、行别、工务段解析失败记录（使用默认值”文件名异常“保存）:\n" + exceptionInfo);
                }
            }
        });
        new Thread(task, "gtdq-gdt-task").start();
        progressBar.setProgress(0.02);

        if (cache.unFinishFilePathList != null && cache.unFinishFilePathList.size() > 0) {
            dialog.setContentText("存在上次入库中断情况，稍后会自动清理脏数据！");
            log.appendText("存在上次入库中断情况，稍后会自动清理脏数据！");
            dialog.show();
        }
    }

    private Cache cache = new Cache();

    /**
     * 根据本地文件记录，获取连接信息、以入库的信息
     */
    public boolean initFileInfo() throws IOException {
        File file = new File(RecordedInfo.recored.recoredFileName);
        if (!file.exists()) {
            return false;
        }
        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String line = "";
        List<String> finishList = new ArrayList<>();
        List<String> unFinishList = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            // 添加换行符
            // tempStream.append(System.getProperty("line.separator"))
            if (line.contains(RecordedInfo.mysqlInfo)) {
                String[] mysqlInfo = line.split(RecordedInfo.mysqlInfo)[1].split(" ");
                cache.url = mysqlInfo[0];
                cache.username = mysqlInfo[1];
                cache.password = mysqlInfo[2];
            }
            if (line.contains(RecordedInfo.filePathPre)) {
                String fileInfo = line.split(RecordedInfo.filePathPre)[1];
                if (line.contains(RecordedInfo.fileState0)) {
                    String dbAndTable = fileInfo.split(RecordedInfo.fileState0)[0];
                    unFinishList.add(dbAndTable);
                } else {
                    String dbAndTable = fileInfo.split(RecordedInfo.fileState1)[0];
                    finishList.add(dbAndTable);
                }
            }
        }
        cache.doneFilePathList = finishList;
        cache.unFinishFilePathList = unFinishList;
        br.close();
        isr.close();
        fis.close();
        return true;
    }


    public static class Cache {
        private String url;
        private String username;
        private String password;
        public List<String> doneFilePathList;
        public List<String> unFinishFilePathList;
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
                MySqlUtil.initConn(dbUrl.trim(), dbUsername.trim(), dbPassword.trim());
            } catch (Exception e) {
                updateMessage("连接到mysql数据库失败,请退出程序重新配置!");
                return false;
            }
            return true;
        }

    }
}

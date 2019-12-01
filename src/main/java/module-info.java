module gdg {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires commons.lang;
    /*以下两个为导入controller和main启动函数的包路径*/
    opens application to javafx.fxml;
    exports application;
    opens application.controller to javafx.fxml;
    exports application.controller;
}
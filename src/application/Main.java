package application;

import java.io.File;
import java.util.concurrent.TimeUnit;

import application.utils.Log;
import application.utils.Util;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * UPDATE employee SET name = CASE id<10 WHEN TRUE THEN '<10' WHEN FALSE THEN
 * '>=10' END
 * 
 * @author mrliu
 *
 */
public class Main extends Application {
	public static Scene scene;

	@Override
	public void start(Stage primaryStage) {
		try {
			System.out.println(new File("").getAbsolutePath());
			System.out.println(getClass().getResource("/Mars.fxml"));
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Mars.fxml"));
			Parent root = fxmlLoader.load();

			scene = new Scene(root);
			// Scene scene = new Scene(root, 600, 500);
			scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
			primaryStage.setTitle("Db Tool");
			primaryStage.setScene(scene);

			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/i.png")));
			
			primaryStage.setOnCloseRequest(event -> {
				primaryStage.close();
				event.consume();
				Util.exit();
			});
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
			primaryStage.close();
			Util.exit();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.fxml.FXMLLoader;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class jmetalator extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() {

    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        primaryStage.setScene(new Scene(root));

        primaryStage.setTitle("jMetalator v1.0 - jMetal Framework Simulator");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() {

    }
}

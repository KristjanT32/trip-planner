package krisapps.tripplanner;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {

    public static Stage window;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("application.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 926, 734);
        stage.setTitle("KrisApps Trip Planner");
        stage.setScene(scene);
        stage.setMinWidth(926);
        stage.setMinHeight(734);
        window = stage;
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
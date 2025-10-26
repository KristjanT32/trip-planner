package krisapps.tripplanner;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PlannerApplication extends javafx.application.Application {

    public static Stage window;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PlannerApplication.class.getResource("application.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1100, 860);
        stage.setTitle("KrisApps Trip Planner");
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(800);
        window = stage;
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
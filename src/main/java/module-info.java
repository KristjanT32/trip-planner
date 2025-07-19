module krisapps.tripplanner.tripplanner {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires com.google.gson;
    requires java.desktop;

    opens krisapps.tripplanner to javafx.fxml;
    exports krisapps.tripplanner;
}
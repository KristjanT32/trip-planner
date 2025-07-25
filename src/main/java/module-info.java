module krisapps.tripplanner {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires com.google.gson;
    requires java.desktop;
    requires org.apache.commons.lang3;

    opens krisapps.tripplanner to javafx.fxml;
    exports krisapps.tripplanner;
}
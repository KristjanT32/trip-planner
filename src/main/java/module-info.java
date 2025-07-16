module krisapps.tripplanner.tripplanner {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;

    opens krisapps.tripplanner.tripplanner to javafx.fxml;
    exports krisapps.tripplanner.tripplanner;
}
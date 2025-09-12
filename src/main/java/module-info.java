module krisapps.tripplanner {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires com.google.gson;
    requires java.desktop;
    requires org.apache.commons.lang3;
    requires org.jetbrains.annotations;

    opens krisapps.tripplanner to javafx.fxml;
    opens krisapps.tripplanner.data.listview.itinerary to javafx.fxml;
    opens krisapps.tripplanner.data.listview.expense_linker to javafx.fxml;
    opens krisapps.tripplanner.data.prompts to javafx.fxml;
    opens krisapps.tripplanner.data.listview.upcoming_trips to javafx.fxml;
    opens krisapps.tripplanner.data.listview.cost_list to javafx.fxml;

    opens krisapps.tripplanner.data.trip to com.google.gson;

    exports krisapps.tripplanner;
    exports krisapps.tripplanner.data;
    opens krisapps.tripplanner.data to com.google.gson, javafx.fxml;
}
module krisapps.tripplanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.httpserver;

    requires org.kordamp.ikonli.javafx;
    requires com.google.gson;
    requires java.desktop;
    requires org.apache.commons.lang3;
    requires org.jetbrains.annotations;
    requires google.http.client.jackson2;
    requires google.api.services.calendar.v3.rev411;
    requires google.oauth.client;
    requires google.api.client;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client;
    requires java.sql;
    requires openhtmltopdf.pdfbox;
    requires javafx.graphics;
    requires javafx.base;
    requires org.apache.pdfbox;
    requires openhtmltopdf.core;

    opens krisapps.tripplanner to javafx.fxml;
    opens krisapps.tripplanner.data.listview.itinerary to javafx.fxml;
    opens krisapps.tripplanner.data.listview.expense_linker to javafx.fxml;
    opens krisapps.tripplanner.data.prompts to javafx.fxml;
    opens krisapps.tripplanner.data.listview.upcoming_trips to javafx.fxml;
    opens krisapps.tripplanner.data.listview.cost_list to javafx.fxml;

    opens krisapps.tripplanner.data.trip to com.google.gson;

    exports krisapps.tripplanner;
    exports krisapps.tripplanner.data;
    exports krisapps.tripplanner.data.trip;
    exports krisapps.tripplanner.misc;

    opens krisapps.tripplanner.data to com.google.gson, javafx.fxml;
    opens krisapps.tripplanner.misc to com.google.gson, javafx.fxml;
    exports krisapps.tripplanner.misc.utils;
    opens krisapps.tripplanner.misc.utils to com.google.gson, javafx.fxml;
}
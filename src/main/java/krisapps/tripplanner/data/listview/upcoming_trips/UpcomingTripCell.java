package krisapps.tripplanner.data.listview.upcoming_trips;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import krisapps.tripplanner.Application;
import krisapps.tripplanner.data.TripUtility;
import krisapps.tripplanner.data.trip.Trip;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class UpcomingTripCell extends ListCell<Trip> {

    @FXML
    private HBox rootPane;

    @FXML
    private Label tripNameLabel;

    @FXML
    private Label tripDateLabel;

    @FXML
    private Label tripDestinationLabel;

    @FXML
    private Label countdownLabel;

    @FXML
    private ProgressBar countdownProgressbar;

    final TripUtility util = TripUtility.getInstance();
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public UpcomingTripCell() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("listview/upcoming_trip_cell.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        tripNameLabel.setStyle("-fx-text-fill: black");
        tripDateLabel.setStyle("-fx-text-fill: black");
        tripDestinationLabel.setStyle("-fx-text-fill: black");
        countdownLabel.setStyle("-fx-text-fill: black");
    }

    @Override
    protected void updateItem(Trip item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            tripNameLabel.setText(item.getTripName());
            tripDateLabel.setText(formatter.format(item.getTripStartDate()));
            tripDestinationLabel.setText(item.getTripDestination());
            countdownLabel.setText(DurationFormatUtils.formatDurationWords(Duration.between(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()), item.getTripStartDate()).toMillis(), true, true));

            setText(null);
            setGraphic(rootPane);
        }
    }
}

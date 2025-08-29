package krisapps.tripplanner.data.listview.upcoming_trips;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import krisapps.tripplanner.Application;
import krisapps.tripplanner.TripPlanner;
import krisapps.tripplanner.data.TripManager;
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
    private Label onLabel;

    @FXML
    private Label destinationPrefix;

    @FXML
    private Button overviewButton;

    @FXML
    private Button editButton;

    @FXML
    private ProgressBar countdownProgressbar;

    final TripManager util = TripManager.getInstance();
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    public UpcomingTripCell() {
        loadFXML();
    }

    @SuppressWarnings("ConstantConditions")
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
        onLabel.setStyle("-fx-text-fill: black");
        destinationPrefix.setStyle("-fx-text-fill: black");

        overviewButton.setOnAction((e) -> {
            TripManager.log("Overview button pressed");
        });
    }

    @Override
    protected void updateItem(Trip item, boolean empty) {
        super.updateItem(item, empty);

        editButton.setOnAction((e) -> {
            TripManager.log("Opening '" + item.getTripName() + "' (" + item.getUniqueID() + ") in Planner");
            TripPlanner.getInstance().openExistingTrip(item, true);
        });

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            if (item.tripDatesSupplied()) {
                Duration duration = Duration.between(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()), item.getTripStartDate());

                tripDateLabel.setText(formatter.format(item.getTripStartDate()));

                if (duration.isNegative() || duration.isZero()) {
                    countdownLabel.setText("");
                } else {
                    countdownLabel.setText(DurationFormatUtils.formatDuration(duration.toMillis(), "dd'd' : HH'h' : mm'm' : ss's'", true));
                }
            } else {
                tripDateLabel.setText("date TBD");
                countdownLabel.setText("");
            }
            tripNameLabel.setText(item.getTripName());
            tripDestinationLabel.setText(item.getTripDestination());

            setText(null);
            setGraphic(rootPane);
        }
    }
}

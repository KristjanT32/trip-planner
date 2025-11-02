package krisapps.tripplanner.data.listview.upcoming_trips;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.TripPlanner;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.TripSettings;
import krisapps.tripplanner.data.dialogs.LoadingDialog;
import krisapps.tripplanner.data.trip.Trip;
import krisapps.tripplanner.misc.utils.GoogleCalendarIntegration;
import krisapps.tripplanner.misc.utils.PopupManager;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private HBox buttonContainer;

    final TripManager util = TripManager.getInstance();
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    final Runnable onDelete;

    public UpcomingTripCell(Runnable onDelete) {
        this.onDelete = onDelete;
        loadFXML();
    }

    @SuppressWarnings("ConstantConditions")
    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("listview/upcoming_trip_cell.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        tripNameLabel.setStyle("-fx-text-fill: black");
        tripDateLabel.setStyle("-fx-text-fill: black");
        tripDestinationLabel.setStyle("-fx-text-fill: black");
        countdownLabel.setStyle("-fx-text-fill: black");
        onLabel.setStyle("-fx-text-fill: black");
        destinationPrefix.setStyle("-fx-text-fill: black");

        buttonContainer.setVisible(false);
        buttonContainer.setManaged(false);
        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                buttonContainer.setVisible(true);
                buttonContainer.setManaged(true);
            } else {
                buttonContainer.setVisible(false);
                buttonContainer.setManaged(false);
            }
        });
    }

    @Override
    protected void updateItem(Trip item, boolean empty) {
        super.updateItem(item, empty);

        deleteButton.setOnAction((e) -> {
            Optional<ButtonType> response = PopupManager.showConfirmation("Delete trip plan", "Are you entirely completely unshakably sure that you wish to erase '" + item.getTripName() + "' from existence?\nThis will wipe all trip data related to the trip, including the settings.\n\nThis cannot be undone.",
                    new ButtonType("Yes, delete", ButtonBar.ButtonData.APPLY),
                    new ButtonType("No, cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
            );
            if (response.isPresent()) {
                if (response.get().getButtonData() == ButtonBar.ButtonData.APPLY) {
                    Optional<ButtonType> deleteEvents = PopupManager.showConfirmation("Delete calendar events?", "Would you also like to delete the calendar events for '" + item.getTripName() + "'?\nThis can also not be undone.",
                            new ButtonType("Yes, delete", ButtonBar.ButtonData.APPLY),
                            new ButtonType("No, keep", ButtonBar.ButtonData.CANCEL_CLOSE)
                    );
                    boolean shouldDeleteEvents = deleteEvents.isPresent() && deleteEvents.get().getButtonData() == ButtonBar.ButtonData.APPLY;

                    LoadingDialog dlg = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_PROGRESSBAR);
                    dlg.setPrimaryLabel("Deleting trip data");
                    dlg.setSecondaryLabel("Please wait...");
                    dlg.show("Deletion in progress", () -> {
                        if (shouldDeleteEvents) {
                            dlg.setSecondaryLabel("Deleting calendar events...");
                            GoogleCalendarIntegration.deleteCalendarEventsForTrip(item);
                        }

                        dlg.setSecondaryLabel("Deleting trip settings...");
                        TripManager.getInstance().deleteSettings(item);

                        dlg.setSecondaryLabel("Deleting trip...");
                        TripManager.getInstance().deleteTrip(item);

                        Platform.runLater(onDelete);
                    });
                }
            }
        });

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
                TripSettings tripSettings = TripManager.getInstance().getSettingsForTrip(item.getUniqueID());

                tripDateLabel.setText(formatter.format(item.getTripStartDate()));

                if (duration.isNegative() || duration.isZero()) {
                    if (item.getTripStartDate().isBefore(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())) && item.getTripEndDate().isAfter(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))) {
                        countdownLabel.setText("Ongoing");
                    } else {
                        countdownLabel.setText("");
                    }
                } else {
                    countdownLabel.setText(DurationFormatUtils.formatDuration(duration.toMillis(), tripSettings.getCountdownFormat().getFormat(), true));
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

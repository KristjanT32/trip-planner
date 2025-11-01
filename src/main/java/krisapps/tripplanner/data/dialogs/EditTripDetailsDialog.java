package krisapps.tripplanner.data.dialogs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.trip.Trip;

import java.io.IOException;

public class EditTripDetailsDialog extends Dialog<Void> {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField nameBox;

    @FXML
    private TextField destinationBox;

    private final Trip trip;


    public EditTripDetailsDialog(Trip t) {
        this.trip = t;

        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/edit_trip_details.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Edit trip details");

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);

        setResultConverter((response) -> {
            if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                if (!nameBox.getText().isEmpty()) {
                    trip.setTripName(nameBox.getText());
                }
                if (!destinationBox.getText().isEmpty()) {
                    trip.setTripDestination(destinationBox.getText());
                }
            }
            return null;
        });

        nameBox.setText(trip.getTripName());
        destinationBox.setText(trip.getTripDestination());
    }


}

package krisapps.tripplanner.data.dialogs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.misc.utils.PopupManager;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class AddOrEditItineraryEntryDialog extends Dialog<Itinerary.ItineraryItem> {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField descriptionBox;

    @FXML
    private Spinner<Integer> dayBox;

    @FXML
    private TextField startField;

    @FXML
    private TextField endField;

    @FXML
    private Label title;

    private final Itinerary.ItineraryItem item;
    private boolean invalid = false;


    /**
     * Creates an Itinerary Entry Edit Dialog.
     *
     * @param item The itinerary entry to edit, or null, if adding a new one.
     * @param edit If <code>true</code>, the dialog will open in edit mode, otherwise a new entry will be created.
     */
    public AddOrEditItineraryEntryDialog(@Nullable Itinerary.ItineraryItem item, int tripDuration, boolean edit) {
        this.item = (item == null ? new Itinerary.ItineraryItem("", -1) : item);

        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/edit_itinerary_entry.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(edit ? "Edit itinerary item details" : "Add itinerary item");
        title.setText(edit ? "Edit itinerary item details" : "Add itinerary item");

        // Trickery to be able to close the dialog manually
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(new ButtonType(edit ? "Apply" : "Add", ButtonBar.ButtonData.APPLY));

        setOnCloseRequest(event -> {
            if (descriptionBox.getText().isEmpty() && invalid) {
                event.consume();
                invalid = false;
            }
        });

        setResultConverter((response) -> {
            if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                if (!descriptionBox.getText().isEmpty()) {
                    this.item.setDescription(descriptionBox.getText());
                } else {
                    PopupManager.showPredefinedPopup(PopupManager.PopupType.ENTRY_DESCRIPTION_MISSING);
                    invalid = true;
                    return null;
                }
                this.item.setDay(dayBox.getValue() == 0 ? -1 : dayBox.getValue());
                try {
                    this.item.setStartTime(new SimpleDateFormat("HH:mm").parse(startField.getText()));
                } catch (ParseException e) {
                    this.item.setStartTime(null);
                }

                try {
                    this.item.setEndTime(new SimpleDateFormat("HH:mm").parse(endField.getText()));
                } catch (ParseException e) {
                    this.item.setEndTime(null);
                }
                return this.item;
            }
            return null;
        });

        descriptionBox.setText(this.item.getDescription());
        startField.setText(this.item.getStartTime() != null ? new SimpleDateFormat("HH:mm").format(this.item.getStartTime()) : "");
        startField.textProperty().addListener((observable, oldValue, newValue) -> {
            startField.setStyle("-fx-text-fill: " + (startField.getText().matches("(?:0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]") ? "black" : "red"));
        });

        endField.setText(this.item.getEndTime() != null ? new SimpleDateFormat("HH:mm").format(this.item.getEndTime()) : "");
        endField.textProperty().addListener((observable, oldValue, newValue) -> {
            endField.setStyle("-fx-text-fill: " + (endField.getText().matches("(?:0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]") ? "black" : "red"));
        });

        dayBox.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, tripDuration));
        dayBox.getValueFactory().setValue(this.item.getDay());
    }


}

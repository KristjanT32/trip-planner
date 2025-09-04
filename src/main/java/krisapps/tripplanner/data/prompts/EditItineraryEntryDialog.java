package krisapps.tripplanner.data.prompts;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.Application;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.Trip;

import java.io.IOException;

public class EditItineraryEntryDialog extends Dialog<Void> {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField descriptionBox;

    @FXML
    private Spinner<Integer> dayBox;

    private final Trip t;
    private final Itinerary.ItineraryItem item;


    public EditItineraryEntryDialog(Trip t, Itinerary.ItineraryItem item) {
        this.t = t;
        this.item = item;

        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("dialogs/edit_itinerary_entry.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Edit itinerary item details");

        // Trickery to be able to close the dialog manually
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);

        setResultConverter((response) -> {
            if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                t.getItinerary().getItems().replace(item.getItemID(), item);
            }
            return null;
        });

        descriptionBox.setText(item.getItemDescription());
        descriptionBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                item.setItemDescription(newValue);
            }
        });

        dayBox.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, (int) t.getTripDuration().toDays()));
        dayBox.getValueFactory().setValue(item.getAssociatedDay());
        dayBox.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue == 0) {
                item.setAssociatedDay(-1);
                return;
            }
            item.setAssociatedDay(newValue);
        }));
    }


}

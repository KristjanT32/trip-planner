package krisapps.tripplanner.data.prompts;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.Application;
import krisapps.tripplanner.data.trip.Trip;

import java.io.IOException;
import java.util.function.UnaryOperator;

public class EditTripDetailsDialog extends Dialog<Void> {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField nameBox;

    @FXML
    private TextField destinationBox;

    @FXML
    private TextField budgetBox;

    private final Trip trip;

    private final UnaryOperator<TextFormatter.Change> numbersOnlyFormatter = (change) -> {
        if (change.getControlNewText().isEmpty()) {
            return change;
        }

        try {
            Double.parseDouble(change.getControlNewText());
            return change;
        } catch (NumberFormatException ignored) {}

        return null;
    };


    public EditTripDetailsDialog(Trip t) {
        this.trip = t;

        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("dialogs/edit_trip_details.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        }
        catch (IOException e) {
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
                if (!budgetBox.getText().isEmpty()) {
                    if (Double.parseDouble(budgetBox.getText()) < 0) {
                        budgetBox.setText("0.0");
                    }
                    trip.setBudget(Double.parseDouble(budgetBox.getText()));
                }
            }
            return null;
        });

        nameBox.setText(trip.getTripName());
        destinationBox.setText(trip.getTripDestination());
        budgetBox.setText(String.valueOf(trip.getExpenseData().getBudget()));

        budgetBox.setTextFormatter(new TextFormatter<>(numbersOnlyFormatter));
    }


}

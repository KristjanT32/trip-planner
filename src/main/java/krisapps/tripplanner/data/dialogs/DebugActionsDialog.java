package krisapps.tripplanner.data.dialogs;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.ProgramSettings;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.trip.ExpenseCategory;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;
import krisapps.tripplanner.misc.DocumentGenerator;
import krisapps.tripplanner.misc.utils.PopupManager;

import java.io.IOException;

public class DebugActionsDialog extends Dialog<ProgramSettings> {

    @FXML
    private VBox rootPane;

    @FXML
    private ChoiceBox<PopupManager.PopupType> popupSelector;

    public DebugActionsDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/debug_actions.fxml"));
            loader.setController(this);
            this.rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Debug Actions");

        getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));

        popupSelector.getItems().clear();
        popupSelector.getItems().addAll(PopupManager.PopupType.values());
    }

    public void showPopup() {
        Platform.runLater(() -> {
            if (popupSelector.getValue() != null) {
                PopupManager.showPredefinedPopup(popupSelector.getValue());
            }
        });
    }

    public void generateTestDocument() {
        Trip test = new Trip("test", "test");
        test.getExpenseData().addExpense(new PlannedExpense(32, "test", ExpenseCategory.UNCATEGORIZED));
        DocumentGenerator.generateTripPlan(test, TripManager.getInstance().getSettings().getDocumentGeneratorOutputFolder());
    }


}

package krisapps.tripplanner.data;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import krisapps.tripplanner.Application;

import java.util.Optional;

public class PopupManager {

    public enum PopupType {
        EXPENSE_NAME_MISSING,
        EXPENSE_AMOUNT_MISSING,
        NOT_IMPLEMENTED,

    }

    public static void showPredefinedPopup(PopupType type) {
        Alert alert = new Alert(null);
        alert.getDialogPane().getStylesheets().add(Application.class.getResource("styles/styles.css").toExternalForm());
        switch (type) {
            case EXPENSE_NAME_MISSING -> {
                alert.setTitle("Cannot add expense entry");
                alert.setContentText("Please fill in the name of your expense and try again.");
                alert.setAlertType(Alert.AlertType.ERROR);
            }
            case EXPENSE_AMOUNT_MISSING -> {
                alert.setTitle("Cannot add expense entry");
                alert.setContentText("Please fill in the value of your expense and try again.");
                alert.setAlertType(Alert.AlertType.ERROR);
            }
            case NOT_IMPLEMENTED -> {
                alert.setTitle("Uh-oh!");
                alert.setContentText("This feature hasn't been implemented yet. Sorry!");
                alert.setAlertType(Alert.AlertType.WARNING);
            }
        }
        alert.setHeaderText(null);
        alert.show();
    }

    public static Optional<ButtonType> showConfirmation(String title, String message, ButtonType optionA, ButtonType optionB) {
        if (!optionA.getButtonData().isCancelButton() && !optionB.getButtonData().isCancelButton()) {
            throw new IllegalArgumentException("Cannot show a confirmation dialog without a cancel option - please ensure that either optionA or optionB is a cancellation ButtonType");
        }

        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.getDialogPane().getStylesheets().add(Application.class.getResource("styles/styles.css").toExternalForm());
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.getButtonTypes().clear();
        a.getButtonTypes().addAll(optionA, optionB);
        return a.showAndWait();
    }


}

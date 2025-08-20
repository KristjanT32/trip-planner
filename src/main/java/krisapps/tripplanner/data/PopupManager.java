package krisapps.tripplanner.data;

import javafx.scene.control.Alert;

public class PopupManager {

    public enum PopupType {
        EXPENSE_NAME_MISSING,
        EXPENSE_AMOUNT_MISSING,

    }

    public static void showPredefinedPopup(PopupType type) {
        Alert alert = new Alert(null);
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
        }
        alert.setHeaderText(null);
        alert.show();
    }


}

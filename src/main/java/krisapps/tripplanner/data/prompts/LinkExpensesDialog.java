package krisapps.tripplanner.data.prompts;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.Application;

import java.io.IOException;

public class LinkExpensesDialog extends Dialog<Void> {

    @FXML
    private VBox rootPane;

    @FXML
    private Button closeButton;

    public LinkExpensesDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("dialogs/link_expenses.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        getDialogPane().setContent(rootPane);
        initModality(Modality.WINDOW_MODAL);

        // Trickery to be able to close the dialog manually
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Node b = getDialogPane().lookupButton(ButtonType.CANCEL);
        b.setVisible(false);
        b.setManaged(false);

        setTitle("Link expenses");
        closeButton.setOnAction(e -> {
            close();
        });
    }
}

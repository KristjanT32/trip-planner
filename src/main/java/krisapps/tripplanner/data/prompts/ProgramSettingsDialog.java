package krisapps.tripplanner.data.prompts;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.ProgramSettings;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class ProgramSettingsDialog extends Dialog<ProgramSettings> {

    @FXML
    private VBox rootPane;

    @FXML
    private TextField symbolField;

    @FXML
    private ToggleButton symbolPositionToggle;

    @FXML
    private ToggleButton autoOpenLastToggle;

    @FXML
    private TextField pathBox;

    public ProgramSettingsDialog(ProgramSettings settings) {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/program_settings.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        setResultConverter((buttonType, _settings) -> {
//            if (buttonType.getButtonData() == ButtonBar.ButtonData.APPLY) {
//                return _settings;
//            } else {
//                return settings;
//            }
//        });


        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Trip Planner Settings");

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);

        symbolPositionToggle.setSelected(settings.currencySymbolPrefixed());
        symbolPositionToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            symbolPositionToggle.setText(!newValue ? "Mode: Suffix" : "Mode: Prefix");
        });

        symbolField.setText(settings.getCurrencySymbol());
        pathBox.setText(settings.getDocumentGeneratorOutputFolder() == null ? "" : settings.getDocumentGeneratorOutputFolder().toString());

        pathBox.textProperty().addListener((observable, oldVal, newVal) -> {
            try {
                Path.of(pathBox.getText());
                pathBox.setStyle("-fx-text-fill: black");
            } catch (InvalidPathException e) {
                pathBox.setStyle("-fx-text-fill: red");
            }
        });
    }


}

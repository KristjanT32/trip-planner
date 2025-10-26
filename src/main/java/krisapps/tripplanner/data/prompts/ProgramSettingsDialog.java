package krisapps.tripplanner.data.prompts;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
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
    private ToggleButton breakPageForDaysToggle;

    @FXML
    private TextField pathBox;

    private final ProgramSettings settings;

    public ProgramSettingsDialog(ProgramSettings settings) {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/program_settings.fxml"));
            loader.setController(this);
            this.settings = settings;
            this.rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        setResultConverter((buttonType) -> {
            if (buttonType.getButtonData() == ButtonBar.ButtonData.APPLY) {
                return this.settings;
            } else {
                return null;
            }
        });


        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Trip Planner Settings");

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);

        symbolPositionToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            symbolPositionToggle.setText(newValue ? "Mode: Prefix" : "Mode: Suffix");
            this.settings.setCurrencySymbolPrefixed(newValue);
        });
        symbolPositionToggle.setText(settings.currencySymbolPrefixed() ? "Mode: Prefix" : "Mode: Suffix");
        symbolPositionToggle.setSelected(settings.currencySymbolPrefixed());

        autoOpenLastToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            autoOpenLastToggle.setText(newValue ? "Enabled" : "Disabled");
            this.settings.setOpenLastTrip(newValue);
        });
        autoOpenLastToggle.setSelected(this.settings.shouldOpenLastTrip());

        breakPageForDaysToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            breakPageForDaysToggle.setText(newValue ? "Enabled" : "Disabled");
            this.settings.setBreakPageForDays(newValue);
        });
        breakPageForDaysToggle.setSelected(this.settings.shouldBreakPageForEachDay());

        symbolField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                this.settings.setCurrencySymbol(newValue);
            }
        });
        symbolField.setText(settings.getCurrencySymbol());

        pathBox.setText(settings.getDocumentGeneratorOutputFolder() == null ? "" : settings.getDocumentGeneratorOutputFolder().toString());
        pathBox.textProperty().addListener((observable, oldVal, newVal) -> {
            try {
                Path.of(pathBox.getText());
                pathBox.setStyle("-fx-text-fill: black");
                this.settings.setDocumentGeneratorOutputFolder(Path.of(pathBox.getText()));
            } catch (InvalidPathException e) {
                pathBox.setStyle("-fx-text-fill: red");
            }
        });
    }


}

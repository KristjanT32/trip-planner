package krisapps.tripplanner.data.dialogs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.ProgramSettings;
import krisapps.tripplanner.data.document_generator.PlanDocumentSettings;

import java.io.IOException;
import java.util.Optional;

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

        symbolField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                this.settings.setCurrencySymbol(newValue);
            }
        });
        symbolField.setText(settings.getCurrencySymbol());
    }

    public void promptShowGeneratorSettings() {
        DocumentSetupDialog dlg = new DocumentSetupDialog(this.settings.getDocumentGeneratorSettings().copy());
        Optional<PlanDocumentSettings> changed = dlg.showAndWait();
        changed.ifPresent(this.settings::setDocumentGeneratorSettings);
    }


}

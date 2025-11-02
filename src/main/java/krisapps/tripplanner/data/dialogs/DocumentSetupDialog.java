package krisapps.tripplanner.data.dialogs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.document_generator.PlanDocumentSettings;
import krisapps.tripplanner.data.document_generator.PlanSection;
import krisapps.tripplanner.data.document_generator.PlanSectionState;
import krisapps.tripplanner.data.listview.document_sections.PlanSectionCellFactory;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;

public class DocumentSetupDialog extends Dialog<PlanDocumentSettings> {

    @FXML
    private VBox rootPane;

    @FXML
    private ListView<PlanSectionState> layoutList;

    @FXML
    private ToggleButton breakPageForDaysToggle;

    @FXML
    private TextField pathBox;

    private final PlanDocumentSettings settings;

    public DocumentSetupDialog(PlanDocumentSettings settings) {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/generate_document.fxml"));
            loader.setController(this);
            this.rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.settings = settings;

        layoutList.setCellFactory(new PlanSectionCellFactory((section) -> {
            // If the modified section is the finances section, modify its children as well, since Finances contains both the financial overview and the expense list.
            if (section.getSection() == PlanSection.FINANCES) {
                this.settings.getIncludeSections().replace(PlanSection.FINANCES, section.isIncluded());
                this.settings.getIncludeSections().replace(PlanSection.BUDGET_OVERVIEW, section.isIncluded());
                this.settings.getIncludeSections().replace(PlanSection.EXPENSE_LIST, section.isIncluded());
            } else {
                this.settings.getIncludeSections().replace(section.getSection(), section.isIncluded());
            }
            layoutList.refresh();
        }));
        layoutList.getItems().addAll(Arrays.stream(PlanSection.values()).map(section -> new PlanSectionState(section, this.settings.getIncludeSections().getOrDefault(section, true))).toList());

        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Plan document generator settings");

        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);

        setResultConverter((response) -> {
            if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                return this.settings;
            } else {
                return null;
            }
        });


        breakPageForDaysToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            breakPageForDaysToggle.setText(newValue ? "Enabled" : "Disabled");
            this.settings.setBreakPageForDays(newValue);
        });
        breakPageForDaysToggle.setSelected(this.settings.shouldBreakPageForEachDay());

        pathBox.setText(settings.getOutputFolder() == null ? "" : settings.getOutputFolder().toString());
        pathBox.textProperty().addListener((observable, oldVal, newVal) -> {
            try {
                Path.of(pathBox.getText());
                pathBox.setStyle("-fx-text-fill: black");
                this.settings.setOutputFolder(Path.of(pathBox.getText()));
            } catch (InvalidPathException e) {
                pathBox.setStyle("-fx-text-fill: red");
            }
        });
    }


}

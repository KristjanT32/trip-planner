package krisapps.tripplanner.data.listview.document_sections;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.document_generator.PlanSectionState;

import java.io.IOException;
import java.util.function.Consumer;

public class PlanSectionCell extends ListCell<PlanSectionState> {

    @FXML
    private VBox rootPane;

    @FXML
    private CheckBox sectionStateToggle;

    @FXML
    private Label sectionNameLabel;

    @FXML
    private Label sectionDescriptionLabel;

    private final Consumer<PlanSectionState> stateChangeCallback;

    public PlanSectionCell(Consumer<PlanSectionState> stateChangeCallback) {
        this.stateChangeCallback = stateChangeCallback;
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("listview/plan_section_cell.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(PlanSectionState item, boolean empty) {
        super.updateItem(item, empty);

        sectionStateToggle.setOnAction((e) -> {
            item.setIncluded(sectionStateToggle.isSelected());
            stateChangeCallback.accept(item);
            this.updateItem(item, false);
        });

        if (!empty) {
            sectionNameLabel.setText(item.getSection().getDisplayName());
            sectionDescriptionLabel.setText(item.getSection().getDescription());
            sectionStateToggle.setSelected(item.isIncluded());

            sectionNameLabel.setStyle("-fx-text-fill: black");
            sectionDescriptionLabel.setStyle("-fx-text-fill: black");

            setGraphic(rootPane);
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}

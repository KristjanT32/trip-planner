package krisapps.tripplanner.data.listview.document_sections;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import krisapps.tripplanner.data.document_generator.PlanSectionState;

import java.util.function.Consumer;

public class PlanSectionCellFactory implements Callback<ListView<PlanSectionState>, ListCell<PlanSectionState>> {

    final Consumer<PlanSectionState> sectionStateToggled;

    public PlanSectionCellFactory(Consumer<PlanSectionState> sectionStateToggled) {
        this.sectionStateToggled = sectionStateToggled;
    }

    @Override
    public ListCell<PlanSectionState> call(ListView<PlanSectionState> param) {
        return new PlanSectionCell(sectionStateToggled);
    }
}

package krisapps.tripplanner.data.listview.cost_list;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import krisapps.tripplanner.Application;

import java.io.IOException;

public class CostListCell extends ListCell<CategoryExpenseSummary> {

    @FXML
    private VBox rootPane;

    @FXML
    private Label totalLabel;

    @FXML
    private Label entryCountLabel;

    @FXML
    private Label categoryNameLabel;

    public CostListCell() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("listview/cost_list_cell.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(CategoryExpenseSummary item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty) {
            categoryNameLabel.setText(item.getCategory().getDisplayName());
            entryCountLabel.setText("Total expenses: " + item.getExpenseCount());

            // TODO: Implement currency symbol setting
            totalLabel.setText(item.getTotalAmount() + "€");

            categoryNameLabel.setStyle("-fx-text-fill: black");
            entryCountLabel.setStyle("-fx-text-fill: black");
            totalLabel.setStyle("-fx-text-fill: black");

            setGraphic(rootPane);
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}

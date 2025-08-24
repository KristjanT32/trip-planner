package krisapps.tripplanner.data.listview.expense_linker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import krisapps.tripplanner.Application;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.trip.PlannedExpense;

import java.io.IOException;

public class ExpenseLinkerCell extends ListCell<PlannedExpense> {

    @FXML
    private HBox rootPane;

    @FXML
    private Label expenseDesc;

    @FXML
    private Label expenseVal;

    @FXML
    private Label expenseType;

    final TripManager util = TripManager.getInstance();

    public ExpenseLinkerCell() {
        loadFXML();
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("listview/expense_linker_expense_cell.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        expenseDesc.setStyle("-fx-text-fill: black");
        expenseVal.setStyle("-fx-text-fill: black");
        expenseType.setStyle("-fx-text-fill: black");
    }

    @Override
    protected void updateItem(PlannedExpense item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            expenseDesc.setText(item.getExpenseSource());
            expenseVal.setText(TripManager.Formatting.formatMoney(item.getAmount(), "€", false));
            expenseType.setText("#" + item.getCategory().name());
            setText(null);
            setGraphic(rootPane);
        }
    }
}

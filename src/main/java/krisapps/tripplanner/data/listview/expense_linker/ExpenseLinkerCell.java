package krisapps.tripplanner.data.listview.expense_linker;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.TripPlanner;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.prompts.EditExpenseDialog;
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
    private Label expenseDay;

    @FXML
    private Label expenseType;

    @FXML
    private Button editExpenseButton;

    final TripManager util = TripManager.getInstance();
    final boolean editable;

    public ExpenseLinkerCell(boolean editable) {
        loadFXML();
        this.editable = editable;
    }

    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("listview/expense_linker_expense_cell.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        expenseDesc.setStyle("-fx-text-fill: black");
        expenseVal.setStyle("-fx-text-fill: black");
        expenseType.setStyle("-fx-text-fill: black");
        expenseDay.setStyle("-fx-text-fill: black");
        editExpenseButton.setVisible(false);

        selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (editable) {
                editExpenseButton.setVisible(newValue);
            } else {
                editExpenseButton.setVisible(false);
            }
        });
    }

    @Override
    protected void updateItem(PlannedExpense item, boolean empty) {
        super.updateItem(item, empty);

        editExpenseButton.setOnAction(event -> {
            if (editable) {
                EditExpenseDialog editDialog = new EditExpenseDialog(TripPlanner.getInstance().getOpenPlan(), item);
                editDialog.showAndWait();
                this.updateItem(item, false);
            }
        });

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            expenseDesc.setText(item.getDescription());
            expenseVal.setText(TripManager.Formatting.formatMoney(item.getAmount(), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));
            expenseType.setText("#" + item.getCategory().name());
            if (item.getDay() != -1) {
                expenseDay.setText("Day #" + item.getDay());
            } else {
                expenseDay.setText("");
            }
            setText(null);
            setGraphic(rootPane);
        }
    }
}

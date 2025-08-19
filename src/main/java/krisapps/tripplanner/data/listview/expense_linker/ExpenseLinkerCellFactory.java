package krisapps.tripplanner.data.listview.expense_linker;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import krisapps.tripplanner.data.trip.PlannedExpense;

public class ExpenseLinkerCellFactory implements Callback<ListView<PlannedExpense>, ListCell<PlannedExpense>> {
    @Override
    public ListCell<PlannedExpense> call(ListView<PlannedExpense> param) {
        return new ExpenseLinkerCell();
    }
}

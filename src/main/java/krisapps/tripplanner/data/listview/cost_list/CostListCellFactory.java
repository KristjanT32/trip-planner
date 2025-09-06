package krisapps.tripplanner.data.listview.cost_list;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class CostListCellFactory implements Callback<ListView<CategoryExpenseSummary>, ListCell<CategoryExpenseSummary>> {
    @Override
    public ListCell<CategoryExpenseSummary> call(ListView<CategoryExpenseSummary> param) {
        return new CostListCell();
    }
}

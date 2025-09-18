package krisapps.tripplanner.data.prompts;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.trip.ExpenseCategory;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.UnaryOperator;

public class EditExpenseDialog extends Dialog<Void> {

    private final UnaryOperator<TextFormatter.Change> numbersOnlyFormatter = (change) -> {
        if (change.getControlNewText().isEmpty()) {
            return change;
        }

        try {
            Double.parseDouble(change.getControlNewText());
            return change;
        } catch (NumberFormatException ignored) {
        }

        return null;
    };

    private final Trip trip;
    private final PlannedExpense expense;

    @FXML
    private VBox rootPane;

    @FXML
    private TextField valueBox;

    @FXML
    private TextField descriptionBox;

    @FXML
    private Spinner<Integer> dayBox;

    @FXML
    private ChoiceBox<String> categoryBox;


    public EditExpenseDialog(Trip t, PlannedExpense expense) {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/edit_expense.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.expense = expense;
        this.trip = t;

        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Edit expense details");

        // Trickery to be able to close the dialog manually
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().getButtonTypes().add(ButtonType.APPLY);

        setResultConverter((response) -> {
            if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                if (valueBox.getText().isEmpty()) {
                    valueBox.setText("0");
                }

                // Apply all data changes
                expense.setAmount(Double.parseDouble(valueBox.getText()));
                if (!descriptionBox.getText().isEmpty()) {
                    expense.setDescription(descriptionBox.getText());
                }
                expense.setCategory(ExpenseCategory.valueOf(categoryBox.getSelectionModel().getSelectedItem()));
                expense.setDay(dayBox.getValue());

                // Refresh the expense entry in the expense data for the trip
                t.getExpenseData().removeExpense(expense.getId());
                t.getExpenseData().addExpense(expense);
            }
            return null;
        });

        valueBox.setTextFormatter(new TextFormatter<>(numbersOnlyFormatter));
        valueBox.setText(String.valueOf(expense.getAmount()));
        valueBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                expense.setAmount(Double.parseDouble(newValue));
            } else {
                expense.setAmount(0.0);
            }
        });

        descriptionBox.setText(expense.getDescription());

        dayBox.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, (int) t.getTripDuration().toDays()));
        dayBox.getValueFactory().setValue(expense.getDay());

        ObservableList<String> items = categoryBox.getItems();
        items.clear();
        items.addAll(Arrays.stream(ExpenseCategory.values()).map(ExpenseCategory::name).toList());
        categoryBox.getSelectionModel().select(expense.getCategory().name());
    }


}

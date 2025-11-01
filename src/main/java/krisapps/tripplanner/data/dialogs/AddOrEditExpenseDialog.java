package krisapps.tripplanner.data.dialogs;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.TripPlanner;
import krisapps.tripplanner.data.ProgramSettings;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.trip.ExpenseCategory;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.UnaryOperator;

public class AddOrEditExpenseDialog extends Dialog<PlannedExpense> {

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

    @FXML
    private Label title;

    //<editor-fold desc="Daily Budget Limit Panel>
    @FXML
    private VBox dailyBudgetPanel;

    @FXML
    private Label budgetLabel;

    @FXML
    private Label expenseTotalLabel;

    @FXML
    private Label budgetValueLabel;
    //</editor-fold>

    final Node applyButton;


    /**
     * Creates an Expense Entry Edit Dialog.
     *
     * @param expense The expense to edit, or null, if creating a new one.
     * @param edit    If <code>true</code>, the dialog will open in edit mode, otherwise a new entry will be created.
     */
    public AddOrEditExpenseDialog(PlannedExpense expense, int tripDuration, boolean edit) {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/edit_expense.fxml"));
            loader.setController(this);
            rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.expense = (expense == null ? new PlannedExpense("", 0.0d) : expense);

        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(edit ? "Edit expense details" : "Add expense");
        title.setText(edit ? "Edit expense details" : "Add expense");

        // Trickery to be able to close the dialog manually
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        ButtonType applyButtonType = new ButtonType(edit ? "Apply" : "Add", ButtonBar.ButtonData.APPLY);
        getDialogPane().getButtonTypes().add(applyButtonType);
        applyButton = getDialogPane().lookupButton(applyButtonType);

        dailyBudgetPanel.managedProperty().addListener((observable, oldValue, newValue) -> {
            getDialogPane().requestLayout();
            getDialogPane().getScene().getWindow().sizeToScene();
        });

        setResultConverter((response) -> {
            if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                if (valueBox.getText().isEmpty()) {
                    valueBox.setText("0");
                }

                // Apply all data changes
                this.expense.setAmount(Double.parseDouble(valueBox.getText()));
                if (!descriptionBox.getText().isEmpty()) {
                    this.expense.setDescription(descriptionBox.getText());
                }
                this.expense.setCategory(ExpenseCategory.valueOf(categoryBox.getSelectionModel().getSelectedItem()));
                this.expense.setDay(dayBox.getValue());

                return this.expense;
            } else {
                return null;
            }
        });

        valueBox.setTextFormatter(new TextFormatter<>(numbersOnlyFormatter));
        valueBox.setText(String.valueOf(this.expense.getAmount()));
        valueBox.textProperty().addListener((observable, oldValue, newValue) -> {
            refreshDailyBudgetPanel();
        });

        descriptionBox.setText(this.expense.getDescription());

        dayBox.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, tripDuration));
        dayBox.getValueFactory().setValue(this.expense.getDay());
        dayBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            refreshDailyBudgetPanel();
        });

        ObservableList<String> items = categoryBox.getItems();
        items.clear();
        items.addAll(Arrays.stream(ExpenseCategory.values()).map(ExpenseCategory::name).toList());
        categoryBox.getSelectionModel().select(this.expense.getCategory().name());
        refreshDailyBudgetPanel();
    }

    private double getCurrentValue() {
        try {
            Double.parseDouble(valueBox.getText());
            return Double.parseDouble(valueBox.getText());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void refreshDailyBudgetPanel() {
        if (TripPlanner.getInstance().getOpenPlan() == null) return;
        if (dayBox.getValue() == null) return;
        Trip currentPlan = TripPlanner.getInstance().getOpenPlan();
        ProgramSettings currentProgramSettings = TripManager.getInstance().getSettings();


        dailyBudgetPanel.setVisible(currentPlan.getExpenseData().getBudgetData().hasDailyBudgetLimit() && dayBox.getValue() > 0);
        dailyBudgetPanel.setManaged(currentPlan.getExpenseData().getBudgetData().hasDailyBudgetLimit() && dayBox.getValue() > 0);

        double expensesPerSelectedDay = currentPlan.getExpenseData().getPlannedExpenses().values().stream().filter(exp -> exp.getDay() == dayBox.getValue() && exp.getId() != this.expense.getId()).mapToDouble(PlannedExpense::getAmount).sum() + getCurrentValue();
        boolean exceededDailyBudget = dayBox.getValue() > 0 && expensesPerSelectedDay > currentPlan.getExpenseData().getBudgetData().getDailyBudget();

        if (exceededDailyBudget) {
            budgetValueLabel.setText(TripManager.Formatting.formatMoney(currentPlan.getExpenseData().getBudgetData().getDailyBudget(), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed())
                    + " (+" + TripManager.Formatting.formatMoney(expensesPerSelectedDay - currentPlan.getExpenseData().getBudgetData().getDailyBudget(), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()) + ")"
            );
        } else {
            budgetValueLabel.setText(TripManager.Formatting.formatMoney(currentPlan.getExpenseData().getBudgetData().getDailyBudget(), currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed()));
        }
        expenseTotalLabel.setText(
                TripManager.Formatting.formatMoney(expensesPerSelectedDay, currentProgramSettings.getCurrencySymbol(), currentProgramSettings.currencySymbolPrefixed())
        );

        if (exceededDailyBudget) {
            dailyBudgetPanel.getStyleClass().add("exceeded");
        } else {
            dailyBudgetPanel.getStyleClass().removeAll("exceeded");
        }
        budgetLabel.setText(exceededDailyBudget ? "Daily budget exceeded: " : "Daily budget limit: ");
        if (applyButton != null) {
            applyButton.setDisable(exceededDailyBudget && currentPlan.getExpenseData().getBudgetData().shouldEnforceDailyBudget());
        }
    }


}

package krisapps.tripplanner.data.dialogs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.ProgramSettings;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.trip.BudgetData;

import java.io.IOException;

public class BudgetSettingsDialog extends Dialog<BudgetData> {

    @FXML
    private final VBox rootPane;

    @FXML
    private Label title;

    @FXML
    private Label splitSpinnerSuffix;

    @FXML
    private Label splitResultLabel;

    @FXML
    private CheckBox limitToggle;

    @FXML
    private CheckBox splitToggle;

    @FXML
    private TextField budgetBox;

    @FXML
    private TextField dailyBudgetBox;

    @FXML
    private Spinner<Integer> splitBetweenSpinner;

    @FXML
    private VBox splitSettings;

    private final BudgetData data;
    private final ProgramSettings settings = TripManager.getInstance().getSettings();

    public BudgetSettingsDialog(String tripName, int partySize, BudgetData data) {
        try {
            FXMLLoader loader = new FXMLLoader(PlannerApplication.class.getResource("dialogs/budget_settings.fxml"));
            loader.setController(this);
            this.rootPane = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        getDialogPane().setContent(rootPane);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Budget Settings");

        getDialogPane().getButtonTypes().add(new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE));
        getDialogPane().getButtonTypes().add(new ButtonType("Apply", ButtonBar.ButtonData.APPLY));

        this.data = data;
        title.setText("Budget settings for \"" + tripName + "\"");

        setResultConverter((response) -> {
            if (response.getButtonData() == ButtonBar.ButtonData.APPLY) {
                return this.data;
            } else {
                return null;
            }
        });

        SpinnerValueFactory<Integer> spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, partySize, partySize, 1);
        splitBetweenSpinner.setValueFactory(spinnerValueFactory);

        splitBetweenSpinner.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue > 0) {
                this.data.setSplitCostsBetween(newValue);
            } else {
                this.data.setSplitCostsBetween(1);
            }
            updateSplitResult();
            updateSpinnerSuffix();
        });

        splitToggle.setSelected(this.data.shouldSplitCosts());
        limitToggle.setSelected(this.data.shouldEnforceDailyBudget());

        splitSettings.setDisable(!splitToggle.isSelected());

        budgetBox.setText(String.valueOf(this.data.getBudget()));
        dailyBudgetBox.setText(String.valueOf(this.data.getDailyBudget()));

        dailyBudgetBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                boolean valid = newValue.matches("[0-9]+(?:\\.[0-9]{2})?");
                dailyBudgetBox.setStyle("-fx-text-fill: " + (valid ? "black" : "red"));

                if (valid) {
                    this.data.setDailyBudget(Double.parseDouble(newValue));
                }
            }
        });

        budgetBox.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                boolean valid = newValue.matches("[0-9]+(?:\\.[0-9]{2})?");
                budgetBox.setStyle("-fx-text-fill: " + (valid ? "black" : "red"));

                if (valid) {
                    this.data.setBudget(Double.parseDouble(newValue));
                    updateSplitResult();
                }
            }
        });

        limitToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.data.setEnforceDailyBudget(newValue);
        });

        splitToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            this.data.setSplitCosts(newValue);
            splitSettings.setDisable(!splitToggle.isSelected());
        });

        updateSpinnerSuffix();
        updateSplitResult();
    }

    private void updateSplitResult() {
        splitResultLabel.setText(TripManager.Formatting.formatMoney(Math.floor(this.data.getBudget() / this.data.getSplitCostsBetween()), settings.getCurrencySymbol(), settings.currencySymbolPrefixed()));
    }

    private void updateSpinnerSuffix() {
        splitSpinnerSuffix.setText(splitBetweenSpinner.getValue() == 1 ? "person" : "people");
    }


}

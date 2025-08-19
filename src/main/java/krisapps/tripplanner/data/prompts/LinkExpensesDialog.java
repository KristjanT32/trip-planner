package krisapps.tripplanner.data.prompts;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import krisapps.tripplanner.Application;
import krisapps.tripplanner.data.listview.expense_linker.ExpenseLinkerCellFactory;
import krisapps.tripplanner.data.trip.PlannedExpense;

import java.io.IOException;

public class LinkExpensesDialog extends Dialog<Void> {

    @FXML
    private VBox rootPane;

    @FXML
    private Label activityNameLabel;

    @FXML
    private Label expenseTotalLabel;

    @FXML
    private ListView<PlannedExpense> expenseList;

    @FXML
    private ListView<PlannedExpense> linkedExpenseList;

    @FXML
    private Button closeButton;

    private String activityName;

    private boolean initialized = false;


    public LinkExpensesDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(Application.class.getResource("dialogs/link_expenses.fxml"));
            loader.setController(this);
            rootPane = loader.load();
            initialized = true;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        getDialogPane().setContent(rootPane);
        initModality(Modality.WINDOW_MODAL);

        expenseList.setCellFactory(new ExpenseLinkerCellFactory());
        linkedExpenseList.setCellFactory(new ExpenseLinkerCellFactory());

        expenseList.getItems().add(new PlannedExpense("test", 100));
        expenseList.getItems().add(new PlannedExpense("test-2", 100));

        linkedExpenseList.getItems().add(new PlannedExpense("linked", 100));
        linkedExpenseList.getItems().add(new PlannedExpense("linked-2", 100));

        // Trickery to be able to close the dialog manually
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Node b = getDialogPane().lookupButton(ButtonType.CANCEL);
        b.setVisible(false);
        b.setManaged(false);

        setTitle("Link expenses");
        activityNameLabel.setText(activityName);
        closeButton.setOnAction(e -> {
            close();
        });
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
        if (initialized) {
            activityNameLabel.setText(activityName);
        }
    }

    public void linkSelectedExpense() {
        System.out.println("Linking selected expense");
    }

    public void unlinkSelectedExpense() {
        System.out.println("Unlinking selected expense");
    }
}

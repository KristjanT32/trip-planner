package krisapps.tripplanner.data.trip;

public class PlannedExpense {
    private int day = -1;
    private double amount;
    private String expenseSource;
    private ExpenseCategory category;

    public PlannedExpense(double amount, String expenseSource, ExpenseCategory category) {
        this.amount = amount;
        this.expenseSource = expenseSource;
        this.category = category;
    }

    public PlannedExpense(String expenseSource, double amount) {
        this.expenseSource = expenseSource;
        this.amount = amount;
        this.category = ExpenseCategory.UNCATEGORIZED;
    }
}

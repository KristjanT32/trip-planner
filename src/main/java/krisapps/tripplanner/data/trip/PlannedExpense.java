package krisapps.tripplanner.data.trip;

import java.util.UUID;

public class PlannedExpense {
    private int day = -1;
    private double amount;
    private String expenseSource;
    private ExpenseCategory category;
    private final UUID expenseID;

    public PlannedExpense(double amount, String expenseSource, ExpenseCategory category) {
        this.amount = amount;
        this.expenseSource = expenseSource;
        this.category = category;
        this.expenseID = UUID.randomUUID();
    }

    public PlannedExpense(String expenseSource, double amount) {
        this.expenseSource = expenseSource;
        this.amount = amount;
        this.category = ExpenseCategory.UNCATEGORIZED;
        this.expenseID = UUID.randomUUID();
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setExpenseSource(String expenseSource) {
        this.expenseSource = expenseSource;
    }

    public void setCategory(ExpenseCategory category) {
        this.category = category;
    }

    public int getDay() {
        return day;
    }

    public double getAmount() {
        return amount;
    }

    public String getExpenseSource() {
        return expenseSource;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public UUID getExpenseID() {
        return expenseID;
    }
}

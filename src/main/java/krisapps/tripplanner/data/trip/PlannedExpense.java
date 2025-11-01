package krisapps.tripplanner.data.trip;

import java.util.UUID;

public class PlannedExpense {
    private int day = -1;
    private double amount;
    private String description;
    private ExpenseCategory category;
    private final UUID id;

    public PlannedExpense(double amount, String description, ExpenseCategory category) {
        this.amount = amount;
        this.description = description;
        this.category = category;
        this.id = UUID.randomUUID();
    }

    public PlannedExpense(String description, double amount) {
        this.description = description;
        this.amount = amount;
        this.category = ExpenseCategory.UNCATEGORIZED;
        this.id = UUID.randomUUID();
    }

    public PlannedExpense(UUID id, ExpenseCategory category, String description, double amount, int day) {
        this.id = id;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.day = day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public UUID getId() {
        return id;
    }

    public PlannedExpense copy() {
        return new PlannedExpense(this.id, this.category, this.description, this.amount, this.day);
    }

    public boolean isDaySet() {
        return day > 0;
    }
}

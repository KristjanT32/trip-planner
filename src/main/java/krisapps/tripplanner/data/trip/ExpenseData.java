package krisapps.tripplanner.data.trip;

import java.util.HashMap;
import java.util.UUID;

public class ExpenseData {
    private double budget;
    private HashMap<UUID, PlannedExpense> plannedExpenses;
    private boolean modified = false;

    public ExpenseData() {
        this.budget = 0.0d;
        this.plannedExpenses = new HashMap<>(0);
        this.modified = false;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
        this.modified = true;
    }

    public HashMap<UUID, PlannedExpense> getPlannedExpenses() {
        return plannedExpenses;
    }

    public void addExpense(PlannedExpense exp) {
        plannedExpenses.put(exp.getExpenseID(), exp);
        this.modified = true;
    }

    public void removeExpense(UUID expenseID) {
        plannedExpenses.remove(expenseID);
        this.modified = true;
    }

    public void setPlannedExpenses(HashMap<UUID, PlannedExpense> plannedExpenses) {
        this.plannedExpenses = plannedExpenses;
        this.modified = true;
    }

    public boolean hasBeenModified() {
        return modified;
    }

    public void resetModifiedFlag() {
        this.modified = false;
    }
}

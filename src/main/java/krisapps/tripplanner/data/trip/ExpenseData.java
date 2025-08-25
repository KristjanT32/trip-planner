package krisapps.tripplanner.data.trip;

import java.util.HashMap;
import java.util.UUID;

public class ExpenseData {
    private double budget;
    private HashMap<UUID, PlannedExpense> plannedExpenses;

    public ExpenseData() {
        this.setBudget(0.0d);
        this.setPlannedExpenses(new HashMap<>(0));
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

    public HashMap<UUID, PlannedExpense> getPlannedExpenses() {
        return plannedExpenses;
    }

    public void addExpense(PlannedExpense exp) {
        plannedExpenses.put(exp.getExpenseID(), exp);
    }

    public void removeExpense(UUID expenseID) {
        plannedExpenses.remove(expenseID);
    }

    public void setPlannedExpenses(HashMap<UUID, PlannedExpense> plannedExpenses) {
        this.plannedExpenses = plannedExpenses;
    }
}

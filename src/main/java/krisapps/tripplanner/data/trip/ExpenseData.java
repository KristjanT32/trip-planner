package krisapps.tripplanner.data.trip;

import java.util.HashMap;
import java.util.UUID;

public class ExpenseData {
    private BudgetData budgetData;
    private HashMap<UUID, PlannedExpense> plannedExpenses;
    private boolean modified = false;

    public ExpenseData() {
        this.plannedExpenses = new HashMap<>(0);
        this.budgetData = new BudgetData();
        this.modified = false;
    }

    public double getBudget() {
        return this.budgetData.getBudget();
    }

    public void setBudget(double budget) {
        this.budgetData.setBudget(budget);
        this.modified = true;
    }

    public HashMap<UUID, PlannedExpense> getPlannedExpenses() {
        return plannedExpenses;
    }

    public void addExpense(PlannedExpense exp) {
        if (plannedExpenses.containsKey(exp.getId())) return;
        plannedExpenses.put(exp.getId(), exp);
        this.modified = true;
    }

    public void updateExpense(UUID expenseID, PlannedExpense expense) {
        plannedExpenses.replace(expenseID, expense);
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

    public double getTotalExpenses() {
        double total = 0.0d;
        for (PlannedExpense e : plannedExpenses.values()) {
            total += e.getAmount();
        }

        return total;
    }

    public boolean hasBeenModified() {
        this.modified = modified || budgetData.hasBeenModified();
        return modified;
    }

    public void resetModifiedFlag() {
        this.modified = false;
        this.budgetData.resetModifiedFlag();
    }

    public BudgetData getBudgetData() {
        return budgetData;
    }

    public void setBudgetData(BudgetData budgetData) {
        this.budgetData = budgetData;
        this.modified = true;
    }
}

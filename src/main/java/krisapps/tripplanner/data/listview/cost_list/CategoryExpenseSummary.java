package krisapps.tripplanner.data.listview.cost_list;

import krisapps.tripplanner.data.trip.ExpenseCategory;
import krisapps.tripplanner.data.trip.PlannedExpense;

import java.util.ArrayList;
import java.util.UUID;

public class CategoryExpenseSummary {
    private ExpenseCategory category;
    private ArrayList<PlannedExpense> expenses;
    private double totalAmount;

    public CategoryExpenseSummary(ExpenseCategory category) {
        this.category = category;
        this.expenses = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public double getTotalAmount() {
        totalAmount = 0.0;
        for (PlannedExpense expense: expenses) {
            totalAmount += expense.getAmount();
        }

        return totalAmount;
    }

    public int getExpenseCount() {
        return expenses.size();
    }

    public ArrayList<PlannedExpense> getExpenses() {
        return expenses;
    }

    public void addExpense(PlannedExpense expense) {
        if (expenses.stream().anyMatch(e -> e.getId().equals(expense.getId()))) { return; }
        expenses.add(expense);
    }

    public void removeExpense(UUID expenseID) {
        expenses.removeIf(e -> e.getId().equals(expenseID));
    }
}

package krisapps.tripplanner.data;

import krisapps.tripplanner.data.trip.PlannedExpense;

import java.util.ArrayList;
import java.util.UUID;

public class DayExpenses {
    private ArrayList<PlannedExpense> expenses;
    private double totalExpenses;
    private final int dayIndex;

    public DayExpenses(int dayIndex) {
        this.expenses = new ArrayList<>();
        this.totalExpenses = 0.0d;
        this.dayIndex = dayIndex;
    }

    public ArrayList<PlannedExpense> getExpenses() {
        return expenses;
    }

    public int getDayIndex() {
        return dayIndex;
    }

    public double getTotalExpenses() {
        totalExpenses = 0.0d;
        for (PlannedExpense expense : expenses) {
            totalExpenses += expense.getAmount();
        }

        return totalExpenses;
    }

    public void addExpense(PlannedExpense e) {
        if (expenses.stream().noneMatch(exp -> exp.getId() == e.getId())) {
            expenses.add(e);
        }
    }

    public void removeExpense(UUID expenseID) {
        expenses.removeIf(e -> e.getId() == expenseID);
    }
}

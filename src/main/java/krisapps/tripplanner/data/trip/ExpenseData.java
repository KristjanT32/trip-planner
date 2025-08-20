package krisapps.tripplanner.data.trip;

import java.util.ArrayList;

public class ExpenseData {
    public double budget;
    public ArrayList<PlannedExpense> plannedExpenses;

    public ExpenseData() {
        this.budget = 0.0d;
        this.plannedExpenses = new ArrayList<>(0);
    }
}

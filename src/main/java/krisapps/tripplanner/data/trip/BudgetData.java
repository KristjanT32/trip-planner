package krisapps.tripplanner.data.trip;

public class BudgetData {
    private double budget = 0d;
    private double dailyBudget = 0d;

    private boolean enforceDailyBudget = false;
    private boolean splitCosts = false;
    private int splitCostsBetween = 1;

    private boolean modified = false;

    public int getSplitCostsBetween() {
        return splitCostsBetween;
    }

    public void setSplitCostsBetween(int splitCostsBetween) {
        this.splitCostsBetween = splitCostsBetween;
        this.modified = true;
    }

    public boolean shouldSplitCosts() {
        return splitCosts;
    }

    public void setSplitCosts(boolean splitCosts) {
        this.splitCosts = splitCosts;
        this.modified = true;
    }

    public boolean shouldEnforceDailyBudget() {
        return enforceDailyBudget;
    }

    public void setEnforceDailyBudget(boolean enforceDailyBudget) {
        this.enforceDailyBudget = enforceDailyBudget;
        this.modified = true;
    }

    public boolean hasBeenModified() {
        return modified;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
        this.modified = true;
    }

    public double getDailyBudget() {
        return dailyBudget;
    }

    public void setDailyBudget(double dailyBudget) {
        this.dailyBudget = dailyBudget;
        this.modified = true;
    }

    public void resetModifiedFlag() {
        this.modified = false;
    }

    public boolean hasDailyBudgetLimit() {
        return this.dailyBudget > 0;
    }
}

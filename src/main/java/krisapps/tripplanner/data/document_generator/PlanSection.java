package krisapps.tripplanner.data.document_generator;

public enum PlanSection {
    SUMMARY("Trip Summary", "General information about the trip."),
    FINANCES("Trip Finances", "The overview and expense list."),
    BUDGET_OVERVIEW("Budget Overview", "A concise overview of the trip's budget and expenses. Doesn't include the expense list."),
    EXPENSE_LIST("Expense List", "A list of all expenses for the trip."),
    ITINERARY("Trip Itinerary", "The itinerary items for the trip in chronological order, along with the associated expenses."),
    COST_DISTRIBUTION("Cost Distribution", "A table showing each category of expenses and the total cost for each category."),
    ;
    private final String displayName;
    private final String description;

    PlanSection(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}


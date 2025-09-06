package krisapps.tripplanner.data.trip;

public enum ExpenseCategory {
    UNCATEGORIZED("Uncategorized"),
    FOOD("Food"),
    ACCOMMODATION("Accommodation"),
    TRANSPORTATION("Transportation"),
    SIGHTSEEING("Sightseeing"),
    ATTRACTIONS("Attractions"),
    FUEL("Fuel"),
    FEES("Fees"),
    MISCELLANEOUS("Miscellaneous"),
    ;

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

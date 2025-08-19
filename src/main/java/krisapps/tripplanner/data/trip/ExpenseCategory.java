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

    private final String name;

    ExpenseCategory(String name) {
        this.name = name;
    }
}

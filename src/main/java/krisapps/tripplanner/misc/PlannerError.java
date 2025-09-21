package krisapps.tripplanner.misc;

public enum PlannerError {
    TRIP_START_AFTER_END("Trip start date cannot be after the end date!"),
    ANY("");
    private String message;

    PlannerError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

package krisapps.tripplanner.data.trip;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class Trip {
    private String tripName;
    private String tripDestination;
    private short partySize;

    private LocalDateTime tripStartDate;
    private LocalDateTime tripEndDate;

    private final ExpenseData expenses;
    private final Itinerary itinerary;

    public final UUID uniqueID;
    private boolean modified = false;

    public Trip(String tripName, String tripDestination) {
        this.tripName = tripName;
        this.tripDestination = tripDestination;
        this.tripStartDate = null;
        this.tripEndDate = null;
        this.partySize = 1;
        this.expenses = new ExpenseData();
        this.itinerary = new Itinerary();
        this.uniqueID = UUID.randomUUID();
        this.modified = false;
    }

    public String getTripName() {
        return tripName;
    }

    public String getTripDestination() {
        return tripDestination;
    }

    public short getPartySize() {
        return partySize;
    }

    public LocalDateTime getTripStartDate() {
        return tripStartDate;
    }

    public LocalDateTime getTripEndDate() {
        return tripEndDate;
    }

    public Duration getTripDuration() {
        if (tripStartDate == null || tripEndDate == null) {
            return Duration.ZERO;
        }
        return Duration.between(tripStartDate, tripEndDate.plusDays(1));
    }

    public ExpenseData getExpenseData() {
        return expenses;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public UUID getUniqueID() {
        return uniqueID;
    }

    public void setBudget(double budget) {
        this.expenses.setBudget(budget);
        this.modified = true;
    }

    public void setPartySize(short partySize) {
        this.partySize = partySize;
        this.modified = true;
    }

    public void setTripStartDate(LocalDateTime tripStartDate) {
        this.tripStartDate = tripStartDate;
        this.modified = true;
    }

    public void setTripEndDate(LocalDateTime tripEndDate) {
        this.tripEndDate = tripEndDate;
        this.modified = true;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
        this.modified = true;
    }

    public void setTripDestination(String tripDestination) {
        this.tripDestination = tripDestination;
        this.modified = true;
    }

    public boolean tripDatesSupplied() {
        return this.tripStartDate != null && this.tripEndDate != null;
    }

    public boolean hasBeenModified() {
        this.modified = itinerary.hasBeenModified() | expenses.hasBeenModified() | modified;
        return modified;
    }

    public void resetModifiedFlag() {
        this.expenses.resetModifiedFlag();
        this.itinerary.resetModifiedFlag();
        this.modified = false;
    }
}

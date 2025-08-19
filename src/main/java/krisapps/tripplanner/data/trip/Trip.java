package krisapps.tripplanner.data.trip;

import java.time.Duration;
import java.time.LocalDateTime;

public class Trip {
    private String tripName;
    private String tripDestination;
    private short partySize;

    private LocalDateTime tripStartDate;
    private LocalDateTime tripEndDate;

    private final ExpenseData expenses;
    private final Itinerary itinerary;

    public Trip(String tripName, String tripDestination) {
        this.tripName = tripName;
        this.tripDestination = tripDestination;
        this.tripStartDate = null;
        this.tripEndDate = null;
        this.partySize = 1;
        this.expenses = new ExpenseData();
        this.itinerary = new Itinerary(this);
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
        if (tripStartDate == null) {
            throw new IllegalStateException("Cannot get trip duration. The start date for this trip has not been initialized.");
        }
        if (tripEndDate == null) {
            throw new IllegalStateException("Cannot get trip duration. The end date for this trip has not been initialized.");
        }
        return Duration.between(tripStartDate, tripEndDate);
    }

    public ExpenseData getExpenses() {
        return expenses;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public void setBudget(double budget) {
        this.expenses.budget = budget;
    }

    public void setPartySize(short partySize) {
        this.partySize = partySize;
    }

    public void setTripStartDate(LocalDateTime tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public void setTripEndDate(LocalDateTime tripEndDate) {
        this.tripEndDate = tripEndDate;
    }

    public boolean tripDatesSupplied() {
        return this.tripStartDate != null && this.tripEndDate != null;
    }
}

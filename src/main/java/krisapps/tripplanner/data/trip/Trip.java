package krisapps.tripplanner.data.trip;

import java.time.LocalDate;

public class Trip {
    private String tripName;
    private String tripDestination;
    private short partySize;

    private LocalDate tripStartDate;
    private LocalDate tripEndDate;

    private final ExpenseData expenses;
    private final Itinerary itinerary;

    public Trip(String tripName, String tripDestination) {
        this.tripName = tripName;
        this.tripDestination = tripDestination;
        this.tripStartDate = null;
        this.tripEndDate = null;
        this.partySize = 1;
        this.expenses = new ExpenseData();
        this.itinerary = new Itinerary();
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

    public LocalDate getTripStartDate() {
        return tripStartDate;
    }

    public LocalDate getTripEndDate() {
        return tripEndDate;
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

    public void setTripStartDate(LocalDate tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public void setTripEndDate(LocalDate tripEndDate) {
        this.tripEndDate = tripEndDate;
    }
}

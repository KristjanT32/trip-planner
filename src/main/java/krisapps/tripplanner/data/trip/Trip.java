package krisapps.tripplanner.data.trip;

import java.util.Date;

public class Trip {
    private String tripName;
    private String tripDestination;
    private short partySize;

    private Date tripStartDate;
    private Date tripEndDate;

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


}

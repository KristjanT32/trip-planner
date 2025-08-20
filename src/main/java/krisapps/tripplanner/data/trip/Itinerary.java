package krisapps.tripplanner.data.trip;

import java.util.LinkedList;
import java.util.UUID;

public class Itinerary {

    public static class ItineraryItem {
        private String itemDescription;
        private int associatedDay;
        private final LinkedList<UUID> associatedExpenses;

        public ItineraryItem(String itemDescription, int associatedDay) {
            this.itemDescription = itemDescription;
            this.associatedDay = associatedDay;
            this.associatedExpenses = new LinkedList<>();
        }

        public String getItemDescription() {
            return itemDescription;
        }

        public int getAssociatedDay() {
            return associatedDay;
        }

        public LinkedList<UUID> getAssociatedExpenses() {
            return associatedExpenses;
        }

        public void setItemDescription(String itemDescription) {
            this.itemDescription = itemDescription;
        }

        public void setAssociatedDay(int associatedDay) {
            this.associatedDay = associatedDay;
        }

        public void addExpense(PlannedExpense expense) {
            associatedExpenses.addLast(expense.getExpenseID());
        }

        public void removeExpense(String expenseDescription) {

        }

        public void removeExpense(UUID expenseID) {
            associatedExpenses.removeIf(p -> p.equals(expenseID));
        }

        // public PlannedExpense[] getAssociatedExpenses() { }
        // public double getTotalCost() { }
    }

    public LinkedList<ItineraryItem> items;
    public Itinerary() {
        this.items = new  LinkedList<ItineraryItem>();
    }

    public void setItems(LinkedList<ItineraryItem> items) {
        this.items = items;
    }

    public void addItem(ItineraryItem item) {
        items.add(item);
    }
}

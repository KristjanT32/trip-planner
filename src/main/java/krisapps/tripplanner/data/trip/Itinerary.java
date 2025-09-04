package krisapps.tripplanner.data.trip;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.UUID;

public class Itinerary {

    public LinkedHashMap<UUID, ItineraryItem> getItems() {
        return items;
    }

    public static class ItineraryItem {
        private String itemDescription;
        private int associatedDay;
        private final LinkedList<UUID> associatedExpenses;
        private final UUID itemID;

        public ItineraryItem(String itemDescription, int associatedDay) {
            this.itemDescription = itemDescription;
            this.associatedDay = associatedDay;
            this.associatedExpenses = new LinkedList<>();
            itemID = UUID.randomUUID();
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
            if (associatedExpenses.contains(expense.getExpenseID())) return;
            associatedExpenses.addLast(expense.getExpenseID());
        }

        public void addExpense(UUID expenseID) {
            if (associatedExpenses.contains(expenseID)) return;
            associatedExpenses.addLast(expenseID);
        }

        public void removeExpense(String expenseDescription) {

        }

        public void removeExpense(UUID expenseID) {
            associatedExpenses.removeIf(p -> p.equals(expenseID));
        }

        public UUID getItemID() {
            return itemID;
        }
    }

    private LinkedHashMap<UUID, ItineraryItem> items;
    private boolean modified = false;

    public Itinerary() {
        this.items = new LinkedHashMap<>();
        this.modified = false;
    }

    public void setItems(LinkedHashMap<UUID, ItineraryItem> items) {
        this.items = items;
        this.modified = true;
    }

    public void addItem(ItineraryItem item) {
        items.putLast(item.getItemID(), item);
        this.modified = true;
    }

    public void removeItem(UUID itemID) {
        items.remove(itemID);
        this.modified = true;
    }

    public boolean hasBeenModified() {
        return modified;
    }

    public void resetModifiedFlag() {
        this.modified = false;
    }
}

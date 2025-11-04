package krisapps.tripplanner.data.trip;

import java.util.*;

public class Itinerary {

    public LinkedHashMap<UUID, ItineraryItem> getItems() {
        return items;
    }

    public static class ItineraryItem {
        private String description;
        private int day;
        private Date startTime;
        private Date endTime;
        private final LinkedList<UUID> linkedExpenses;
        private final UUID id;
        private boolean modified = false;

        public ItineraryItem(String description, int day) {
            this.description = description;
            this.day = day;
            this.startTime = null;
            this.linkedExpenses = new LinkedList<>();
            this.id = UUID.randomUUID();
            this.modified = false;
        }

        public ItineraryItem(LinkedList<UUID> linkedExpenses, UUID id, Date endTime, Date startTime, int day, String description) {
            this.linkedExpenses = linkedExpenses;
            this.id = id;
            this.endTime = endTime;
            this.startTime = startTime;
            this.day = day;
            this.description = description;
            this.modified = false;
        }

        public String getDescription() {
            return description;
        }

        public int getDay() {
            return day;
        }

        public LinkedList<UUID> getLinkedExpenses() {
            return linkedExpenses;
        }

        public void setDescription(String description) {
            this.description = description;
            this.modified = true;
        }

        public void setDay(int day) {
            this.day = day;
            this.modified = true;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
            this.modified = true;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
            this.modified = true;
        }

        public void linkExpense(PlannedExpense expense) {
            if (linkedExpenses.contains(expense.getId())) return;
            linkedExpenses.addLast(expense.getId());
            this.modified = true;
        }

        public void linkExpense(UUID expenseID) {
            if (linkedExpenses.contains(expenseID)) return;
            linkedExpenses.addLast(expenseID);
            this.modified = true;
        }

        public void setLinkedExpenses(List<UUID> linkedExpenses) {
            this.linkedExpenses.clear();
            this.linkedExpenses.addAll(linkedExpenses);
            this.modified = true;
        }

        public double getExpenseTotal(Trip trip) {
            double totalExpenses = 0.0d;
            for (PlannedExpense exp : linkedExpenses.stream().map(exp -> trip.getExpenseData().getPlannedExpenses().get(exp)).toList()) {
                totalExpenses += exp.getAmount();
            }
            return Math.floor(totalExpenses);
        }

        public void unlinkExpense(UUID expenseID) {
            boolean edited = linkedExpenses.removeIf(p -> p.equals(expenseID));
            this.modified = this.modified || edited;
        }

        public void unlinkAllExpenses() {
            this.linkedExpenses.clear();
            this.modified = true;
        }

        public UUID getId() {
            return id;
        }

        public boolean hasBeenModified() {
            return this.modified;
        }

        public void resetModifiedFlag() {
            this.modified = false;
        }

        public ItineraryItem copy() {
            return new ItineraryItem(linkedExpenses, id, endTime, startTime, day, description);
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
        items.putLast(item.getId(), item);
        this.modified = true;
    }

    public void updateItem(UUID itemID, ItineraryItem item) {
        items.replace(itemID, item);
        this.modified = true;
    }

    public void removeItem(UUID itemID) {
        items.remove(itemID);
        this.modified = true;
    }

    /**
     * Checks whether the Itinerary or its ItineraryItems have been modified.
     *
     * @return <code>true</code> if the Itinerary or its ItineraryItems have been modified, <code>false</code> otherwise.
     */
    public boolean hasBeenModified() {
        this.modified = this.modified || this.items.values().stream().anyMatch(ItineraryItem::hasBeenModified);
        return modified;
    }

    /**
     * Resets the 'modified' flag of the Itinerary, as well as its ItineraryItems
     */
    public void resetModifiedFlag() {
        this.modified = false;
        for (ItineraryItem item : this.items.values()) {
            item.resetModifiedFlag();
        }
    }
}

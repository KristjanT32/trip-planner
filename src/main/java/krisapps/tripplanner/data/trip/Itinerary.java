package krisapps.tripplanner.data.trip;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.UUID;

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

        public ItineraryItem(String description, int day) {
            this.description = description;
            this.day = day;
            this.startTime = null;
            this.linkedExpenses = new LinkedList<>();
            this.id = UUID.randomUUID();
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
        }

        public void setDay(int day) {
            this.day = day;
        }

        public void setStartTime(Date startTime) {
            this.startTime = startTime;
        }

        public Date getStartTime() {
            return startTime;
        }

        public Date getEndTime() {
            return endTime;
        }

        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }

        public void linkExpense(PlannedExpense expense) {
            if (linkedExpenses.contains(expense.getId())) return;
            linkedExpenses.addLast(expense.getId());
        }

        public void linkExpense(UUID expenseID) {
            if (linkedExpenses.contains(expenseID)) return;
            linkedExpenses.addLast(expenseID);
        }

        public double getExpenseTotal(Trip trip) {
            double totalExpenses = 0.0d;
            for (PlannedExpense exp : linkedExpenses.stream().map(exp -> trip.getExpenseData().getPlannedExpenses().get(exp)).toList()) {
                totalExpenses += exp.getAmount();
            }
            return Math.floor(totalExpenses);
        }

        public void unlinkExpense(UUID expenseID) {
            linkedExpenses.removeIf(p -> p.equals(expenseID));
        }

        public UUID getId() {
            return id;
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

    public boolean hasBeenModified() {
        return modified;
    }

    public void resetModifiedFlag() {
        this.modified = false;
    }
}

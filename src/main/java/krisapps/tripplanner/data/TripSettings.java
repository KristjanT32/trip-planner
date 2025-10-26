package krisapps.tripplanner.data;

import java.util.concurrent.TimeUnit;

public class TripSettings {
    private boolean calendarIntegrationEnabled = false;
    private boolean reminderEnabled = false;
    private TimeUnit reminderUnit;
    private int reminderValue;
    private String calendarEventID = "";
    private boolean modified = false;

    private boolean countdownEnabled = false;
    private CountdownFormat countdownFormat = CountdownFormat.DEFAULT;

    public TripSettings() {
        this.calendarIntegrationEnabled = false;
        this.reminderEnabled = false;
        this.reminderUnit = TimeUnit.MINUTES;
        this.reminderValue = -1;
        this.calendarEventID = "";
        this.modified = false;
    }

    public TripSettings(boolean calendarIntegrationEnabled, boolean reminderEnabled, TimeUnit reminderUnit, int reminderValue, String calendarEventID, boolean modified) {
        this.calendarIntegrationEnabled = calendarIntegrationEnabled;
        this.reminderEnabled = reminderEnabled;
        this.reminderUnit = reminderUnit;
        this.reminderValue = reminderValue;
        this.calendarEventID = calendarEventID;
        this.modified = modified;
    }

    public boolean isCalendarIntegrationEnabled() {
        return calendarIntegrationEnabled;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public TimeUnit getReminderUnit() {
        return reminderUnit;
    }

    public int getReminderValue() {
        return reminderValue;
    }

    public void setCalendarIntegrationEnabled(boolean calendarIntegrationEnabled) {
        this.calendarIntegrationEnabled = calendarIntegrationEnabled;
        this.modified = true;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
        this.modified = true;
    }

    public void setReminderUnit(TimeUnit reminderUnit) {
        this.reminderUnit = reminderUnit;
        this.modified = true;
    }

    public void setReminderValue(int reminderValue) {
        this.reminderValue = reminderValue;
        this.modified = true;
    }

    public void setReminder(int value, TimeUnit unit) {
        this.reminderEnabled = true;
        this.reminderValue = value;
        this.reminderUnit = unit;
        this.modified = true;
    }

    public void setCountdownEnabled(boolean countdownEnabled) {
        this.countdownEnabled = countdownEnabled;
        this.modified = true;
    }

    public void setCountdownFormat(CountdownFormat countdownFormat) {
        this.countdownFormat = countdownFormat;
        this.modified = true;
    }

    public boolean isCountdownEnabled() {
        return countdownEnabled;
    }

    public CountdownFormat getCountdownFormat() {
        return countdownFormat;
    }

    public boolean hasReminder() {
        return this.reminderValue != -1 && this.reminderValue != 0;
    }

    public void resetModifiedFlag() {
        this.modified = false;
    }

    public boolean haveBeenModified() {
        return modified;
    }

    public String getCalendarEventID() {
        return calendarEventID;
    }

    public void setCalendarEventID(String calendarEventID) {
        this.calendarEventID = calendarEventID;
        this.modified = true;
    }

    public boolean calendarEventsCreated() {
        return !this.calendarEventID.isBlank();
    }
}

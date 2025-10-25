package krisapps.tripplanner.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;
import krisapps.tripplanner.misc.LocalDateTimeTypeAdapter;
import krisapps.tripplanner.misc.LocalDateTypeAdapter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class TripManager {

    private static final Gson gson = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();
    private static TripManager instance;
    private final File dataFile = new File(System.getProperty("user.home") + File.separator + "TripPlanner Data" + File.separator + "data.json");
    private boolean initialized = false;

    private TripManager() {
    }

    public static TripManager getInstance() {
        if (instance == null) {
            instance = new TripManager();
        }
        return instance;
    }

    public static void log(String msg) {
        if (msg.toLowerCase().contains("failed") || msg.toLowerCase().contains("error") || msg.toLowerCase().contains("fail") || msg.toLowerCase().contains("couldn't") || msg.toLowerCase().contains("could not")) {
            System.out.println(String.format("[%s TripPlanner/ERROR]: ", Formatting.formatDate(Date.from(Instant.now()), true)) + msg);
        } else {
            System.out.println(String.format("[%s TripPlanner/INFO]: ", Formatting.formatDate(Date.from(Instant.now()), true)) + msg);
        }
    }

    public void init() {
        if (initialized) return;
        purgeInvalidExpenses();

        initialized = true;
    }

    private void purgeInvalidExpenses() {
        Data data = getData();
        ArrayList<Trip> trips = data.getTrips();
        boolean modified = false;

        log("Checking for invalid expense entries in itineraries...");
        for (Trip t : trips) {
            for (Itinerary.ItineraryItem item : t.getItinerary().getItems().values()) {
                for (UUID expense : item.getLinkedExpenses()) {
                    if (getExpenseByID(t, expense) != null) continue;

                    log("Invalid expense entry '" + expense + "' found in linked expenses for '" + item.getDescription() + "' - removing");
                    item.getLinkedExpenses().remove(expense);
                    modified = true;
                }
            }
        }
        if (modified) {
            data.setTrips(trips);
            saveData(data);
        } else {
            log("All good. No invalid entries found.");
        }
    }

    public ArrayList<Trip> getTrips() {
        Data d = getData();
        return d.getTrips();
    }

    public void updateTrip(Trip trip) {
        Data data = getData();
        ArrayList<Trip> trips = data.getTrips();

        boolean updated = trips.removeIf((t) -> t.getUniqueID().equals(trip.getUniqueID()));
        trips.add(trip);

        data.setTrips(trips);
        saveData(data);

        if (updated) {
            log("Updated trip '" + trip.getTripName() + "' (" + trip.getUniqueID() + ")");
        } else {
            log("Created new trip '" + trip.getTripName() + "' (" + trip.getUniqueID() + ")");
        }
    }

    public void updateTripSettings(Trip trip, ProgramSettings.TripSettings tripSettings) {
        Data data = getData();
        ProgramSettings programSettings = data.getSettings();

        boolean updated = programSettings.setTripSettings(trip.getUniqueID(), tripSettings);

        data.setSettings(programSettings);
        saveData(data);

        if (updated) {
            log("Updated trip settings for '" + trip.getTripName() + "' (" + trip.getUniqueID() + ")");
        } else {
            log("Added trip settings for '" + trip.getTripName() + "' (" + trip.getUniqueID() + ")");
        }
    }

    public void addExpense(Trip trip, PlannedExpense expense) {
        trip.getExpenseData().addExpense(expense);
    }

    public void removeExpense(Trip trip, UUID expenseID) {
        // Unlink expense from all itinerary items before deletion
        if (isExpenseLinked(trip, expenseID)) {
            log("Found itinerary items with expense marked for deletion. Purging.");
            for (Itinerary.ItineraryItem item : trip.getItinerary().getItems().values()) {
                if (item.getLinkedExpenses().contains(expenseID)) {
                    item.unlinkExpense(expenseID);
                    log("Removed '" + expenseID + "' from '" + item.getDescription() + "'");
                }
            }
        }

        trip.getExpenseData().removeExpense(expenseID);
    }

    public Itinerary.ItineraryItem getItineraryItemByID(Trip t, UUID id) {
        return t.getItinerary().getItems().getOrDefault(id, null);
    }

    public PlannedExpense getExpenseByID(Trip t, UUID id) {
        return t.getExpenseData().getPlannedExpenses().getOrDefault(id, null);
    }

    public boolean isExpenseLinked(Trip t, UUID expenseID) {
        return t.getItinerary().getItems().values().stream().anyMatch(item -> item.getLinkedExpenses().contains(expenseID));
    }

    public void linkExpense(Trip trip, UUID expenseID, UUID itineraryItemID) {
        trip.getItinerary().getItems().computeIfPresent(itineraryItemID, (id, itineraryItem) -> {
            itineraryItem.linkExpense(expenseID);
            return itineraryItem;
        });
    }

    public void unlinkExpense(Trip trip, UUID expenseID, UUID itineraryItemID) {
        trip.getItinerary().getItems().computeIfPresent(itineraryItemID, (id, itineraryItem) -> {
            itineraryItem.unlinkExpense(expenseID);
            return itineraryItem;
        });
    }

    public void saveData(Data data) {

        if (!dataFile.exists()) {
            createDataFile();
        }

        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dataFile, false), StandardCharsets.UTF_16));

            writer.write(gson.toJson(data));
            writer.close();
        } catch (IOException e) {
            log("Data saving failed - " + e.getMessage());
        }
    }

    /**
     * Loads the saved data from disk.
     *
     * @return The data
     */
    private Data getData() {

        if (!dataFile.exists()) {
            firstTimeFileSetup();
        }

        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(dataFile), StandardCharsets.UTF_16);
            JsonReader reader = new JsonReader(inputStreamReader);
            Data output = gson.fromJson(reader, Data.class);
            if (output == null) {
                output = new Data();
            }
            return output;
        } catch (IOException e) {
            log("Failed to retrieve data from data file: " + e.getMessage());
            return new Data();
        }
    }

    // Saving values
    public void setValue(String key, Object val) {
        Data data = getData();
        if (data.getSavedValues().containsKey(key)) {
            data.getSavedValues().replace(key, val);
        } else {
            data.getSavedValues().put(key, val);
        }
        saveData(data);
    }

    public Object getValue(String key) {
        return getData().getSavedValues().getOrDefault(key, "");
    }

    public Object getValue(String key, Object defaultValue) {
        return getData().getSavedValues().getOrDefault(key, defaultValue);
    }

    private void firstTimeFileSetup() {
        log("No files found, initializing first-time setup.");

        try {
            log("Creating a data directory at: " + Path.of(System.getProperty("user.home") + File.separator + "TripPlanner Data"));
            Files.createDirectory(Path.of(System.getProperty("user.home") + File.separator + "TripPlanner Data"));
        } catch (IOException e) {
            log("Failed to create data directory: " + e.getMessage());
        }

        try {
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }
            log("Files successfully created.");
        } catch (IOException e) {
            log("Failed to create file: " + e.getMessage());
        }
    }

    private void createDataFile() {
        try {
            if (!dataFile.exists()) {
                dataFile.createNewFile();
                Data data = new Data();
                saveData(data);
            }
        } catch (IOException e) {
            log("Could not create a new data file - " + e.getMessage());
        }
    }

    public ProgramSettings getSettings() {
        return getData().getSettings();
    }

    public static class Formatting {

        public static DecimalFormat decimalFormatter = new DecimalFormat("#.##");

        public static String generateDurationString(Date start, Date current, boolean showZeros, boolean withWords) {
            Instant startInstant = start.toInstant();
            Instant endInstant = current.toInstant();

            Duration dur = Duration.between(startInstant, endInstant);

            long days = Math.abs(dur.toDays());
            long hours = Math.abs(dur.minusDays(days).toHours());
            long minutes = Math.abs(dur.minusDays(days).minusHours(hours).toMinutes());
            long seconds = Math.abs(dur.minusDays(days).minusHours(hours).minusMinutes(minutes).toSeconds());

            if (!showZeros) {
                if (withWords) {
                    return (days > 0 ? (int) days + " days, " : "") + (hours > 0 ? (int) hours + " hours, " : "") + (minutes > 0 ? (int) minutes + " minutes, " : "") + (seconds > 0 ? (int) seconds + " seconds" : "");
                } else {
                    return (days > 0 ? (int) days + ":" : "") + (hours > 0 ? (int) hours + ":" : "") + (minutes > 0 ? (int) minutes + ":" : "") + (seconds > 0 ? (int) seconds + ":" : "");
                }
            } else {
                if (withWords) {
                    return String.format("%s hours, %s minutes and %s seconds", (int) hours, (int) minutes, (int) seconds);
                } else {
                    return String.format("%s:%s:%s", formatTimeUnit((int) hours), formatTimeUnit((int) minutes), formatTimeUnit((int) seconds));
                }
            }
        }

        public static String formatDate(Date date, boolean withTime) {

            if (date == null) {
                return "N/A";
            }

            DateTimeFormatter dateOnly = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter dateAndTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            if (withTime) {
                return dateAndTime.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
            } else {
                return dateOnly.format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
            }
        }

        public static String formatMoney(double money, String symbol, boolean symbolIsPrefix) {
            String[] doubleString = String.valueOf(money).split("\\.");
            if (symbolIsPrefix) {
                return symbol + " " + doubleString[0] + "." + (Long.parseLong(doubleString[1]) <= 9 ? "0" + Long.parseLong(doubleString[1]) : Long.parseLong(doubleString[1]));
            } else {
                return doubleString[0] + "." + (Long.parseLong(doubleString[1]) <= 9 ? "0" + Long.parseLong(doubleString[1]) : Long.parseLong(doubleString[1])) + " " + symbol;
            }
        }

        public static String formatTimeUnit(int unit) {
            return unit <= 9
                    ? "0" + unit
                    : String.valueOf(unit);
        }

        public static String formatTime(Date date) {
            if (date == null) {
                return "N/A";
            }
            return DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
        }

        public static String getNumberSuffix(int number) {
            return switch (String.valueOf(number).charAt(String.valueOf(number).length() - 1)) {
                case 1 -> "st";
                case 2 -> "nd";
                case 3 -> "rd";
                default -> "th";
            };
        }

        public Date dateFromJSON(String date) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            try {
                return format.parse(date);
            } catch (ParseException e) {
                log("Failed to parse a date from '" + date + "'");
                return null;
            }
        }
    }
}

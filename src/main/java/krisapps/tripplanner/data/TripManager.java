package krisapps.tripplanner.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;
import com.google.gson.stream.JsonReader;
import javafx.util.Pair;
import krisapps.tripplanner.data.trip.ExpenseCategory;
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
import java.util.*;
import java.util.logging.Level;

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

    public static void log(String msg, Level level) {
        System.out.println(String.format("[%s TripPlanner/%s]: ", Formatting.formatDate(Date.from(Instant.now()), true), level.getName()) + msg);
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

    // <editor-fold desc="Data access">

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

    public ArrayList<Trip> getTrips() {
        Data d = getData();
        return d.getTrips();
    }

    public Trip getTripByID(UUID id) {
        return getTrips().stream().filter(t -> t.getUniqueID().equals(id)).findFirst().orElse(null);
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

    public void updateTripSettings(Trip trip, TripSettings tripSettings) {
        Data data = getData();

        boolean updated = data.setTripSettings(trip.getUniqueID(), tripSettings);
        saveData(data);

        if (updated) {
            log("Updated trip settings for '" + trip.getTripName() + "' (" + trip.getUniqueID() + ")");
        } else {
            log("Added trip settings for '" + trip.getTripName() + "' (" + trip.getUniqueID() + ")");
        }
    }

    public void updateProgramSettings(ProgramSettings programSettings) {
        Data data = getData();
        data.setSettings(programSettings);
        saveData(data);
        log("Updated program settings");
    }

    /**
     * Deletes the trip settings for the supplied trip.
     *
     * @param trip The trip whose settings are to be deleted
     */
    public void deleteSettings(Trip trip) {
        Data d = getData();
        d.setTripSettings(trip.getUniqueID(), null);
        saveData(d);
        log("Deleted trip settings for '" + trip.getTripName() + "' (" + trip.getUniqueID() + ")");
    }

    /**
     * Deletes the supplied trip data.
     * This will not delete the trip settings.
     *
     * @param trip The trip to delete
     */
    public void deleteTrip(Trip trip) {
        Data d = getData();
        d.getTrips().removeIf((t) -> t.getUniqueID().equals(trip.getUniqueID()));
        saveData(d);
        log("Deleted trip '" + trip.getTripName() + "' (" + trip.getUniqueID() + ")");
    }

    public PlannedExpense getExpenseByID(Trip t, UUID id) {
        return t.getExpenseData().getPlannedExpenses().getOrDefault(id, null);
    }

    public TripSettings getTripSettings(UUID tripID) {
        Data data = getData();
        return data.getTripSettings(tripID);
    }

    public TripSettings getSettingsForTrip(UUID tripID) {
        return getTripSettings(tripID);
    }

    /**
     * Checks whether the supplied expense is linked to any itinerary entries.
     *
     * @param t         The trip whose expense is supplied
     * @param expenseID The unique ID of the expense to check
     * @return <code>true</code> if the expense is linked, <code>false</code> otherwise
     */
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

    // </editor-fold>

    /**
     * Contains various methods for querying statistics data about a trip.
     */
    public static class Statistics {
        /**
         * Returns the cheapest day in the trip.
         * If no such day can be found, a Pair<-1, -1.0d> will be returned.
         *
         * @param t The trip to check.
         * @return A pair containing the day and the amount spent on that day.
         */
        public static Pair<Integer, Double> getCheapestDay(Trip t) {
            HashMap<Integer, Double> dayExpenses = new HashMap<>();
            for (PlannedExpense exp : t.getExpenseData().getPlannedExpenses().values()) {
                if (exp.getDay() <= 0) continue;
                dayExpenses.put(exp.getDay(), dayExpenses.getOrDefault(exp.getDay(), 0.0d) + exp.getAmount());
            }

            return dayExpenses.entrySet().stream().min(Comparator.comparingDouble(Map.Entry::getValue)).map(e -> new Pair<>(e.getKey(), e.getValue())).orElse(new Pair<>(-1, -1.0d));
        }

        /**
         * Returns the most expensive day in the trip.
         * If no such day can be found, a Pair<-1, -1.0d> will be returned.
         *
         * @param t The trip to check.
         * @return A pair containing the day and the amount spent on that day.
         */
        public static Pair<Integer, Double> getMostExpensiveDay(Trip t) {
            HashMap<Integer, Double> dayExpenses = new HashMap<>();
            for (PlannedExpense exp : t.getExpenseData().getPlannedExpenses().values()) {
                if (exp.getDay() <= 0) continue;
                dayExpenses.put(exp.getDay(), dayExpenses.getOrDefault(exp.getDay(), 0.0d) + exp.getAmount());
            }

            return dayExpenses.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).map(e -> new Pair<>(e.getKey(), e.getValue())).orElse(new Pair<>(-1, -1.0d));
        }

        /**
         * Returns a summary object with data about expenses of the supplied category.
         *
         * @param category The expense category to summarize.
         * @param t        The trip whose expenses are to be summarized.
         * @return A {@link CategoryExpenseSummary} object containing the summary data.
         */
        public static CategoryExpenseSummary getExpensesFor(ExpenseCategory category, Trip t) {
            CategoryExpenseSummary summary = new CategoryExpenseSummary(category);
            for (PlannedExpense e : t.getExpenseData().getPlannedExpenses().values()) {
                if (e.getCategory() == category) {
                    summary.addExpense(e);
                }
            }
            return summary;
        }

        /**
         * Returns a list of summaries for all expense categories in the supplied trip.
         *
         * @param t The trip whose expenses are to be summarized.
         * @return A list of {@link CategoryExpenseSummary} objects containing the summary data.
         */
        public static ArrayList<CategoryExpenseSummary> getExpenseSummariesFor(Trip t) {
            ArrayList<CategoryExpenseSummary> summaries = new ArrayList<>();
            for (ExpenseCategory category : ExpenseCategory.values()) {
                if (t.getExpenseData().getPlannedExpenses().values().stream().anyMatch(expense -> expense.getCategory() == category)) {
                    summaries.add(getExpensesFor(category, t));
                }
            }
            return summaries;
        }

        /**
         * Returns the expense total for the supplied trip.
         *
         * @param t The trip to get the expense total for.
         * @return The sum of all expenses for the trip.
         */
        public static double getExpenseTotalFor(Trip t) {
            return t.getExpenseData().getPlannedExpenses().values().stream().mapToDouble(PlannedExpense::getAmount).sum();
        }

        /**
         * Returns the daily expense average for the supplied trip.
         *
         * @param t The trip to get the daily expense average for.
         * @return The average daily expenses for the trip.
         */
        public static double getDailyExpenseAverageFor(Trip t) {
            HashMap<Integer, Double> dayExpenses = new HashMap<>();
            for (PlannedExpense exp : t.getExpenseData().getPlannedExpenses().values()) {
                if (exp.getDay() <= 0) continue;
                dayExpenses.put(exp.getDay(), dayExpenses.getOrDefault(exp.getDay(), 0.0d) + exp.getAmount());
            }
            double total = dayExpenses.values().stream().mapToDouble(Double::doubleValue).sum();
            return Double.isNaN(total / dayExpenses.size()) ? 0.0d : total / dayExpenses.size();
        }

        public static double getSplitCost(Trip trip) {
            if (trip.getExpenseData().getBudgetData().shouldSplitCosts()) {
                return Math.floor(trip.getExpenseData().getTotalExpenses() / trip.getExpenseData().getBudgetData().getSplitCostsBetween());
            } else {
                return trip.getExpenseData().getTotalExpenses();
            }
        }
    }

    /**
     * Contains various methods for formatting data.
     */
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
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            if (symbolIsPrefix) {
                return symbol + decimalFormat.format(money);
            } else {
                return decimalFormat.format(money) + symbol;
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

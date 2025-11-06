package krisapps.tripplanner.data.document_generator;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.util.Diagnostic;
import com.openhtmltopdf.util.XRLog;
import com.openhtmltopdf.util.XRLogger;
import javafx.application.Platform;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.dialogs.LoadingDialog;
import krisapps.tripplanner.data.trip.ExpenseCategory;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;
import krisapps.tripplanner.misc.utils.PopupManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;

public class DocumentGenerator {

    private static final String EXPENSE_ROW_TEMPLATE = """
                  <tr>
                    <td>{{description}}</td>
                    <td>{{category}}</td>
                    <td>{{amount}}</td>
                    <td>{{day}}</td>
                  </tr>
            """;

    private static final String ITINERARY_ROW_TEMPLATE = """
                  <div class="day{{shouldBreak}}" {{minmaxMarker}}>
                         <h3>Day {{dayNumber}}</h3>
                         <ul>
                           {{activities}}
                         </ul>
                  </div>
            """;

    private static final String ITINERARY_ACTIVITY_TEMPLATE = """
                <li>
                  <strong class="{{align}}">{{period}}</strong> {{description}}
                  {{linkedExpenses}}
                </li>
            """;

    private static final String ITINERARY_ACTIVITY_LINKED_EXPENSES_TEMPLATE = """
                <ul class="linked-expenses">
                  {{rows}}
                </ul>
            """;

    private static final String LINKED_EXPENSE_ROW_TEMPLATE = """
                <table width="100%">
                    <tr>
                      <td style="text-align: left;">{{description}}</td>
                      <td style="text-align: right;"><b>{{amount}}</b></td>
                    </tr>
                  </table>
            """;

    private static final String EXPENSE_CATEGORY_ROW = """
                <tr>
                    <td>{{category}}</td>
                    <td>{{expenses}}</td>
                    <td>{{total}}</td>
                </tr>
            """;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    private static int getTimeInMinutes(Date date) {
        if (date == null) return -1;
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return ldt.getHour() * 60 + ldt.getMinute();
    }

    private static StringBuilder buildItinerary(Trip trip) {
        StringBuilder itineraryTableContent = new StringBuilder();
        LinkedHashMap<Integer, LinkedList<Itinerary.ItineraryItem>> daysToActivities = new LinkedHashMap<>();

        Pair<Integer, Double> cheapestDay = TripManager.Statistics.getCheapestDay(trip);
        Pair<Integer, Double> mostExpensiveDay = TripManager.Statistics.getMostExpensiveDay(trip);

        trip.getItinerary().getItems().forEach((uuid, itineraryItem) -> {
            LinkedList<Itinerary.ItineraryItem> itineraryItems = daysToActivities.getOrDefault(itineraryItem.getDay(), new LinkedList<>());
            itineraryItems.add(itineraryItem);
            daysToActivities.put(itineraryItem.getDay(), itineraryItems);
        });

        for (Map.Entry<Integer, LinkedList<Itinerary.ItineraryItem>> entry : daysToActivities.entrySet()) {
            entry.getValue().sort(Comparator.comparing((e) -> {
                if (e.getStartTime() != null) {
                    return getTimeInMinutes(e.getStartTime());
                } else {
                    return getTimeInMinutes(e.getEndTime());
                }
            }));
        }

        int index = 0;

        for (Map.Entry<Integer, LinkedList<Itinerary.ItineraryItem>> item : daysToActivities.sequencedEntrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getKey)).toList()) {
            StringBuilder dayActivities = new StringBuilder();
            for (Itinerary.ItineraryItem itineraryItem : item.getValue()) {
                // Build linked expense list
                StringBuilder expenseList = new StringBuilder();
                for (UUID linkedExpense : itineraryItem.getLinkedExpenses()) {
                    PlannedExpense exp = trip.getExpenseData().getPlannedExpenses().get(linkedExpense);
                    expenseList.append(
                            LINKED_EXPENSE_ROW_TEMPLATE
                                    .replace("{{description}}", exp.getDescription())
                                    .replace("{{amount}}", TripManager.Formatting.formatMoney(exp.getAmount(), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()))
                    );
                }

                String timePeriod = "";
                String startSection = (itineraryItem.getStartTime() != null ? new SimpleDateFormat("HH:mm").format(itineraryItem.getStartTime()) : "...");
                String endSection = (itineraryItem.getEndTime() != null ? new SimpleDateFormat("HH:mm").format(itineraryItem.getEndTime()) : "...");
                if (startSection.equals("...") && endSection.equals("...")) {
                    timePeriod = "";
                } else {
                    timePeriod = (startSection + (!startSection.isBlank() && !endSection.isBlank() ? " - " : "") + endSection);
                }

                // Build day activities entry
                dayActivities.append(
                        ITINERARY_ACTIVITY_TEMPLATE
                                .replace("{{align}}", timePeriod.startsWith("...") ? "right" : "left")
                                .replace("{{period}}", timePeriod)
                                .replace("{{description}}", itineraryItem.getDescription())
                                .replace("{{linkedExpenses}}",
                                        !itineraryItem.getLinkedExpenses().isEmpty()
                                                ? ITINERARY_ACTIVITY_LINKED_EXPENSES_TEMPLATE
                                                .replace("{{rows}}", expenseList.toString())
                                                : ""
                                )

                );
            }
            itineraryTableContent.append(
                    ITINERARY_ROW_TEMPLATE
                            .replace("{{shouldBreak}}", TripManager.getInstance().getSettings().getDocumentGeneratorSettings().shouldBreakPageForEachDay() && index > 0 ? " break" : "")
                            .replace("{{dayNumber}}", String.valueOf(item.getKey()))
                            .replace("{{activities}}", dayActivities.toString())
                            .replace("{{minmaxMarker}}", item.getKey().intValue() == cheapestDay.getKey() ? "id=\"cheapest\"" : (item.getKey().intValue() == mostExpensiveDay.getKey() ? "id=\"most_expensive\"" : ""))
            ).append("\n");
            index++;
        }
        return itineraryTableContent;
    }

    private static StringBuilder buildCostDistribution(Trip trip) {
        StringBuilder costDistributionContent = new StringBuilder();
        LinkedHashMap<ExpenseCategory, ArrayList<PlannedExpense>> sortedExpenses = new LinkedHashMap<>();
        for (PlannedExpense exp : trip.getExpenseData().getPlannedExpenses().values()) {
            ArrayList<PlannedExpense> plannedExpenses = sortedExpenses.getOrDefault(exp.getCategory(), new ArrayList<>());
            plannedExpenses.add(exp);
            sortedExpenses.put(exp.getCategory(), plannedExpenses);
        }

        HashMap<Double, String> sortedEntries = new HashMap<>();

        for (Map.Entry<ExpenseCategory, ArrayList<PlannedExpense>> entry : sortedExpenses.entrySet()) {
            double sum = 0.0d;
            for (PlannedExpense exp : entry.getValue()) {
                sum += exp.getAmount();
            }
            sum = Math.floor(sum);

            sortedEntries.put(sum, EXPENSE_CATEGORY_ROW
                    .replace("{{category}}", entry.getKey().getDisplayName())
                    .replace("{{total}}", decimalFormat.format(sum))
                    .replace("{{expenses}}", String.valueOf(entry.getValue().size())));
        }

        for (Map.Entry<Double, String> entry : sortedEntries.entrySet().stream().sorted(Collections.reverseOrder(Comparator.comparingDouble(Map.Entry::getKey))).toList()) {
            costDistributionContent.append(entry.getValue()).append("\n");
        }
        return costDistributionContent;
    }

    private static StringBuilder buildExpenseList(Trip trip) {
        StringBuilder expenseTableContent = new StringBuilder();
        List<Map.Entry<UUID, PlannedExpense>> sortedExpenses = trip.getExpenseData().getPlannedExpenses().entrySet().stream().sorted(Comparator.comparingInt(entry -> {
            if (entry.getValue().getDay() > 0) {
                return entry.getValue().getDay();
            } else {
                return Integer.MAX_VALUE;
            }
        })).toList();


        for (Map.Entry<UUID, PlannedExpense> entry : sortedExpenses) {
            expenseTableContent.append(
                    EXPENSE_ROW_TEMPLATE
                            .replace("{{description}}", entry.getValue().getDescription())
                            .replace("{{category}}", entry.getValue().getCategory().getDisplayName())
                            .replace("{{amount}}", decimalFormat.format(entry.getValue().getAmount()))
                            .replace("{{day}}", entry.getValue().getDay() != -1 ? String.valueOf(entry.getValue().getDay()) : "N/A")
            );
            expenseTableContent.append("\n");
        }
        return expenseTableContent;
    }

    private static String fillSummary(String htmlTemplate, Trip trip) {
        htmlTemplate = htmlTemplate.replace("{{tripName}}", trip.getTripName());
        htmlTemplate = htmlTemplate.replace("{{tripDestination}}", trip.getTripDestination());
        htmlTemplate = htmlTemplate.replace("{{tripStartDate}}", formatter.format(trip.getTripStartDate()));
        htmlTemplate = htmlTemplate.replace("{{tripEndDate}}", formatter.format(trip.getTripEndDate()));
        htmlTemplate = htmlTemplate.replace("{{partySize}}", trip.getPartySize() == 1 ? trip.getPartySize() + " person" : trip.getPartySize() + " people");
        htmlTemplate = htmlTemplate.replace("{{generationDate}}", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date.from(Instant.now())));
        return htmlTemplate;
    }


    /**
     * Generates a trip plan PDF document for the supplied trip instance.
     *
     * @param trip   The trip to generate the plan document for.
     * @param output The location where the file will be generated.
     */
    public static void generateTripPlan(Trip trip, Path output) {
        final PlanDocumentSettings settings = TripManager.getInstance().getSettings().getDocumentGeneratorSettings();

        if (trip == null) {
            throw new IllegalArgumentException("Cannot generate trip plan for null trip");
        }
        if (output == null) {
            throw new IllegalArgumentException("Cannot generate trip plan without an output directory");
        }
        if (!output.toFile().isDirectory()) {
            throw new IllegalArgumentException("The output path must point to a directory");
        }

        if (trip.getItinerary().getItems().isEmpty() && trip.getExpenseData().getPlannedExpenses().isEmpty()) {
            Platform.runLater(() -> {
                PopupManager.showPredefinedPopup(PopupManager.PopupType.PLAN_DATA_MISSING);
            });
            return;
        }

        if (!trip.tripDatesSupplied()) {
            Platform.runLater(() -> {
                PopupManager.showPredefinedPopup(PopupManager.PopupType.PLAN_DATA_MISSING);
            });
            return;
        }

        TripManager.log("Generating trip plan document for '" + trip.getTripName() + "'...");
        TripManager.log("Omitting the following sections: " + settings.getIncludeSections().entrySet().stream().filter(e -> !e.getValue()).map(Map.Entry::getKey).toList());
        long start = System.currentTimeMillis();

        XRLog.setLoggerImpl(new XRLogger() {
            @Override
            public void log(String s, Level level, String s1) {
                TripManager.log(s1, level);
            }

            @Override
            public void log(String s, Level level, String s1, Throwable throwable) {
                TripManager.log("==[Exception thrown]=========================================");
                TripManager.log(throwable.getMessage());
                TripManager.log(s1, level);
                TripManager.log("=============================================================");
            }

            @Override
            public void setLevel(String s, Level level) {
                // Ignored
            }

            @Override
            public boolean isLogLevelEnabled(Diagnostic diagnostic) {
                return diagnostic.getLevel().equals(Level.WARNING) || diagnostic.getLevel().equals(Level.SEVERE);
            }
        });

        LoadingDialog dlg = new LoadingDialog(LoadingDialog.LoadingOperationType.INDETERMINATE_PROGRESSBAR);
        dlg.setPrimaryLabel("Creating document...");
        dlg.setSecondaryLabel("Starting generation...");
        dlg.show("Please wait...", () -> {
            String htmlTemplate = "";
            try {
                dlg.setSecondaryLabel("Reading template...");
                htmlTemplate = new String(PlannerApplication.class.getResourceAsStream("pdf_generator/trip_plan_template.html").readAllBytes());
            } catch (IOException e) {
                TripManager.log("--------------------------------------");
                TripManager.log("Error reading trip_plan_template.html");
                TripManager.log(e.getMessage());
                TripManager.log("--------------------------------------");
            }


            try (OutputStream os = new FileOutputStream(output + File.separator + "%s-trip-plan.pdf".formatted(trip.getTripName().toLowerCase().replaceAll(" ", "")))) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                dlg.setSecondaryLabel("Generating plan...");

                // Fill metadata
                if (settings.getIncludeSections().get(PlanSection.SUMMARY)) {
                    htmlTemplate = fillSummary(htmlTemplate, trip);
                    htmlTemplate = htmlTemplate.replace("{{summary-visible}}", "");
                } else {
                    htmlTemplate = htmlTemplate.replace("{{summary-visible}}", "hidden");
                }

                // Finances
                if (settings.getIncludeSections().get(PlanSection.FINANCES)) {
                    htmlTemplate = htmlTemplate.replace("{{financial-overview-visible}}", "");
                    // Budget Overview
                    if (settings.getIncludeSections().get(PlanSection.BUDGET_OVERVIEW)) {
                        Pair<Integer, Double> cheapestDay = TripManager.Statistics.getCheapestDay(trip);
                        Pair<Integer, Double> mostExpensiveDay = TripManager.Statistics.getMostExpensiveDay(trip);

                        htmlTemplate = htmlTemplate.replace("{{budget}}", TripManager.Formatting.formatMoney(trip.getExpenseData().getBudget(), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));
                        htmlTemplate = htmlTemplate.replace("{{totalExpenses}}", TripManager.Formatting.formatMoney(Math.floor(trip.getExpenseData().getTotalExpenses()), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));
                        htmlTemplate = htmlTemplate.replace("{{remainingBudget}}", TripManager.Formatting.formatMoney(Math.floor(trip.getExpenseData().getBudget() - Math.floor(trip.getExpenseData().getTotalExpenses())), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));

                        String dailyBudget = trip.getExpenseData().getBudgetData().getDailyBudget() <= 0 ? "N/A" : TripManager.Formatting.formatMoney(trip.getExpenseData().getBudgetData().getDailyBudget(), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed());
                        htmlTemplate = htmlTemplate.replace("{{dailyBudget}}", dailyBudget);
                        htmlTemplate = htmlTemplate.replace("{{cheapestDay}}", cheapestDay.getValue() == -1 ? "N/A" : TripManager.Formatting.formatMoney(Math.floor(cheapestDay.getValue()), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));
                        htmlTemplate = htmlTemplate.replace("{{mostExpensiveDay}}", mostExpensiveDay.getValue() == -1 ? "N/A" : TripManager.Formatting.formatMoney(Math.floor(mostExpensiveDay.getValue()), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));

                        if (trip.getExpenseData().getBudgetData().shouldSplitCosts()) {
                            htmlTemplate = htmlTemplate.replace("{{splitBetween}}", trip.getExpenseData().getBudgetData().getSplitCostsBetween() + (trip.getExpenseData().getBudgetData().getSplitCostsBetween() == 1 ? " person" : " people"));
                            htmlTemplate = htmlTemplate.replace("{{splitCost}}", TripManager.Formatting.formatMoney(TripManager.Statistics.getSplitCost(trip), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));

                            htmlTemplate = htmlTemplate.replace("{{cost-splitting-visible}}", "");
                        } else {
                            htmlTemplate = htmlTemplate.replace("{{cost-splitting-visible}}", "hidden");
                        }

                        htmlTemplate = htmlTemplate.replace("{{budget-summary-visible}}", "");
                    } else {
                        htmlTemplate = htmlTemplate.replace("{{budget-summary-visible}}", "hidden");
                    }

                    // Expense table
                    if (settings.getIncludeSections().get(PlanSection.EXPENSE_LIST)) {
                        StringBuilder expenseTableContent = buildExpenseList(trip);
                        htmlTemplate = htmlTemplate.replace("{{plannedExpenses}}", expenseTableContent.toString());
                        htmlTemplate = htmlTemplate.replace("{{expenses-visible}}", "");
                    } else {
                        htmlTemplate = htmlTemplate.replace("{{plannedExpenses}}", "");
                        htmlTemplate = htmlTemplate.replace("{{expenses-visible}}", "hidden");
                    }
                } else {
                    htmlTemplate = htmlTemplate.replace("{{financial-overview-visible}}", "hidden");
                    htmlTemplate = htmlTemplate.replace("{{budget-summary-visible}}", "hidden");
                    htmlTemplate = htmlTemplate.replace("{{expenses-visible}}", "hidden");
                }

                // Itinerary table
                if (settings.getIncludeSections().get(PlanSection.ITINERARY)) {
                    StringBuilder itineraryTableContent = buildItinerary(trip);
                    htmlTemplate = htmlTemplate.replace("{{itineraryDays}}", itineraryTableContent.toString());
                    htmlTemplate = htmlTemplate.replace("{{itinerary-visible}}", "");
                } else {
                    htmlTemplate = htmlTemplate.replace("{{itineraryDays}}", "");
                    htmlTemplate = htmlTemplate.replace("{{itinerary-visible}}", "hidden");
                }

                // Cost distribution
                if (settings.getIncludeSections().get(PlanSection.COST_DISTRIBUTION)) {
                    StringBuilder costDistributionContent = buildCostDistribution(trip);
                    htmlTemplate = htmlTemplate.replace("{{categoryExpenses}}", costDistributionContent.toString());
                    htmlTemplate = htmlTemplate.replace("{{cost-distribution-visible}}", "");
                } else {
                    htmlTemplate = htmlTemplate.replace("{{categoryExpenses}}", "");
                    htmlTemplate = htmlTemplate.replace("{{cost-distribution-visible}}", "hidden");
                }

                htmlTemplate = htmlTemplate.replace("{{currencySymbol}}", TripManager.getInstance().getSettings().getCurrencySymbol());

                // Load font
                Path fontFile = Files.createTempFile("noto", ".ttf");
                Files.copy(PlannerApplication.class.getResourceAsStream("pdf_generator/fonts/NotoSans.ttf"), fontFile, StandardCopyOption.REPLACE_EXISTING);

                builder.useFastMode();
                builder.useFont(fontFile.toFile(), "Noto Sans", FontWeight.NORMAL.getWeight(), BaseRendererBuilder.FontStyle.NORMAL, false);
                builder.withHtmlContent(htmlTemplate, PlannerApplication.class.getResource("pdf_generator/").toExternalForm());
                builder.toStream(os);
                builder.run();
                fontFile.toFile().deleteOnExit();
                TripManager.log("Trip plan generated in " + (System.currentTimeMillis() - start) + "ms");
            } catch (Throwable t) {
                TripManager.log("Error generating trip plan: " + t.getMessage());
            }
        });
    }

}

package krisapps.tripplanner.misc;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import javafx.scene.text.FontWeight;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.prompts.LoadingDialog;
import krisapps.tripplanner.data.trip.ExpenseCategory;
import krisapps.tripplanner.data.trip.Itinerary;
import krisapps.tripplanner.data.trip.PlannedExpense;
import krisapps.tripplanner.data.trip.Trip;

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
                  <div class="day{{shouldBreak}}">
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
                <li>{{description}} ({{amount}})</li>
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

    /**
     * Generates a trip plan PDF document for the supplied trip instance.
     *
     * @param trip   The trip to generate the plan document for.
     * @param output The location where the file will be generated.
     */
    public static void generateTripPlan(Trip trip, Path output) {
        if (trip == null) {
            throw new IllegalArgumentException("Cannot generate trip plan for null trip");
        }
        if (output == null) {
            throw new IllegalArgumentException("Cannot generate trip plan without an output directory");
        }
        if (!output.toFile().isDirectory()) {
            throw new IllegalArgumentException("The output path must point to a directory");
        }

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
                htmlTemplate = htmlTemplate.replace("{{tripName}}", trip.getTripName());
                htmlTemplate = htmlTemplate.replace("{{tripDestination}}", trip.getTripDestination());
                htmlTemplate = htmlTemplate.replace("{{tripStartDate}}", formatter.format(trip.getTripStartDate()));
                htmlTemplate = htmlTemplate.replace("{{tripEndDate}}", formatter.format(trip.getTripEndDate()));
                htmlTemplate = htmlTemplate.replace("{{partySize}}", trip.getPartySize() == 1 ? trip.getPartySize() + " person" : trip.getPartySize() + " people");
                htmlTemplate = htmlTemplate.replace("{{currencySymbol}}", TripManager.getInstance().getSettings().getCurrencySymbol());
                htmlTemplate = htmlTemplate.replace("{{generationDate}}", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date.from(Instant.now())));

                // Financial info
                htmlTemplate = htmlTemplate.replace("{{budget}}", TripManager.Formatting.formatMoney(trip.getExpenseData().getBudget(), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));
                htmlTemplate = htmlTemplate.replace("{{totalExpenses}}", TripManager.Formatting.formatMoney(Math.floor(trip.getExpenseData().getTotalExpenses()), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));
                htmlTemplate = htmlTemplate.replace("{{remainingBudget}}", TripManager.Formatting.formatMoney(Math.floor(trip.getExpenseData().getBudget() - Math.floor(trip.getExpenseData().getTotalExpenses())), TripManager.getInstance().getSettings().getCurrencySymbol(), TripManager.getInstance().getSettings().currencySymbolPrefixed()));


                // Expense table
                StringBuilder expenseTableContent = new StringBuilder();
                for (Map.Entry<UUID, PlannedExpense> entry : trip.getExpenseData().getPlannedExpenses().entrySet()) {
                    expenseTableContent.append(
                            EXPENSE_ROW_TEMPLATE
                                    .replace("{{description}}", entry.getValue().getDescription())
                                    .replace("{{category}}", entry.getValue().getCategory().getDisplayName())
                                    .replace("{{amount}}", decimalFormat.format(entry.getValue().getAmount()))
                                    .replace("{{day}}", entry.getValue().getDay() != -1 ? String.valueOf(entry.getValue().getDay()) : "N/A")
                    );
                    expenseTableContent.append("\n");
                }
                htmlTemplate = htmlTemplate.replace("{{plannedExpenses}}", expenseTableContent.toString());

                // Itinerary table
                StringBuilder itineraryTableContent = new StringBuilder();
                LinkedHashMap<Integer, LinkedList<Itinerary.ItineraryItem>> daysToActivities = new LinkedHashMap<>();

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

                for (Map.Entry<Integer, LinkedList<Itinerary.ItineraryItem>> item : daysToActivities.sequencedEntrySet()) {
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
                                    .replace("{{shouldBreak}}", TripManager.getInstance().getSettings().shouldBreakPageForEachDay() && index > 0 ? " break" : "")
                                    .replace("{{dayNumber}}", String.valueOf(item.getKey()))
                                    .replace("{{activities}}", dayActivities.toString())
                    ).append("\n");
                    index++;
                }
                htmlTemplate = htmlTemplate.replace("{{itineraryDays}}", itineraryTableContent.toString());

                // Cost distribution
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

                htmlTemplate = htmlTemplate.replace("{{categoryExpenses}}", costDistributionContent.toString());

                // Load font
                Path fontFile = Files.createTempFile("noto", ".ttf");
                Files.copy(PlannerApplication.class.getResourceAsStream("pdf_generator/fonts/NotoSans.ttf"), fontFile, StandardCopyOption.REPLACE_EXISTING);

                builder.useFastMode();
                builder.useFont(fontFile.toFile(), "Noto Sans", FontWeight.NORMAL.getWeight(), BaseRendererBuilder.FontStyle.NORMAL, false);
                builder.withHtmlContent(htmlTemplate, PlannerApplication.class.getResource("pdf_generator/").toExternalForm());
                builder.toStream(os);
                builder.run();
                fontFile.toFile().deleteOnExit();
                TripManager.log("Done");
            } catch (Throwable t) {
                TripManager.log("Error generating trip plan: " + t.getMessage());
            }
        });
    }

}

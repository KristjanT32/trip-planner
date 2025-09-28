package krisapps.tripplanner.misc;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import krisapps.tripplanner.PlannerApplication;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.trip.Trip;

import java.io.*;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GoogleCalendarIntegration {
    private static final String APPLICATION_NAME = "Trip Planner";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY = "google/tokens";
    private static final List<String> ACCESS_SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE_PATH = "google/credentials.json";

    private static final DateTimeFormatter GOOGLE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    private static Calendar calendarService;
    private static boolean initialized = false;

    // Google's authorization wizardry
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = PlannerApplication.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, ACCESS_SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        // returns an authorized Credential object.
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void initialize() {
        if (initialized) return;
        final NetHttpTransport HTTP_TRANSPORT;
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            calendarService =
                    new Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                            .setApplicationName(APPLICATION_NAME)
                            .build();
            initialized = true;
            TripManager.log("Successfully initialized the Calendar Service.");
        } catch (GeneralSecurityException | IOException e) {
            TripManager.log("Calendar Service authorization failed: " + e.getMessage());
        }
    }

    /**
     * Configures and creates the calendar event for the supplied trip, with the supplied reminder and the description.
     *
     * @param t           The trip to create the calendar events for.
     * @param reminder    Optional reminder for the calendar event.
     * @param description The event description, or if blank, will default to "Trip to [tripDestination]"
     * @return The ID for the newly created event.
     */
    public static String createCalendarEventForTrip(Trip t, EventReminder reminder, String description) {
        Event tripEvent = new Event();
        EventDateTime startTime = new EventDateTime();
        startTime.setDateTime(DateTime.parseRfc3339(GOOGLE_DATE_FORMATTER.format(t.getTripStartDate().atZone(ZoneId.systemDefault()))));

        EventDateTime endTime = new EventDateTime();
        endTime.setDateTime(DateTime.parseRfc3339(GOOGLE_DATE_FORMATTER.format(t.getTripEndDate().atZone(ZoneId.systemDefault()))));

        Event.ExtendedProperties ext = new Event.ExtendedProperties();
        ext.setShared(Map.of("eventType", "trip-planner-event", "plannerEventId", t.getUniqueID().toString()));

        tripEvent.setSummary(t.getTripName());
        tripEvent.setDescription(description.isEmpty() ? String.format("Trip to %s\n", t.getTripDestination()) : description);
        tripEvent.setStart(startTime);
        tripEvent.setEnd(endTime);
        tripEvent.setExtendedProperties(ext);
        tripEvent.setLocation(t.getTripDestination());
        tripEvent.setCreated(new DateTime(Date.from(Instant.now())));

        Event.Reminders reminders = new Event.Reminders();
        reminders.put("reminder", reminder);
        tripEvent.setReminders(reminders);

        try {
            return calendarService.events().insert("primary", tripEvent).execute().getId();
        } catch (IOException e) {
            TripManager.log("Failed to create calendar event for trip: " + e.getMessage());
        }
        TripManager.log("Trip calendar event created successfully.");
        return null;
    }

    /**
     * Gets the eventID for the supplied trip's calendar events.
     *
     * @param t The trip whose calendar events' ID to query.
     * @return The eventID for the supplied trip's calendar events.
     */
    public static Optional<Event> getEventIdForTripEvent(Trip t) {
        List<Event> events = getTripPlannerCalendarEvents();
        return events.stream().filter(ev -> ev.getExtendedProperties().containsKey("plannerEventId") && ev.getExtendedProperties().get("plannerEventId").equals(t.getUniqueID().toString())).findFirst();
    }

    /**
     * Gets all calendar events created by the Planner.
     *
     * @return A list of {@link Event}s created by the Planner.
     */
    public static List<Event> getTripPlannerCalendarEvents() {
        try {
            return calendarService.events().list("primary").setSharedExtendedProperty(List.of("eventType=trip-planner-event")).execute().getItems();
        } catch (IOException e) {
            TripManager.log("Failed to retrieve calendar events: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Deletes the calendar events created for the supplied trip.
     * This will first load the calendar event ID from the saved data, and then interface with Google's Calendar API.
     *
     * @param currentPlan The trip object whose calendar events to wipe.
     * @return True if successful, false otherwise.
     */
    public static boolean deleteCalendarEventsForTrip(Trip currentPlan) {
        String eventId = TripManager.getInstance().getSettings().getTripSettings(currentPlan.getUniqueID()).getCalendarEventID();
        try {
            calendarService.events().delete("primary", eventId).execute();
            return true;
        } catch (IOException e) {
            TripManager.log("Failed to delete calendar events for: " + currentPlan.getTripName() + " (" + currentPlan.getUniqueID() + ") - supplied ID: " + eventId);
            return false;
        }
    }
}

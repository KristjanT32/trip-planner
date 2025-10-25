package krisapps.tripplanner.misc;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import krisapps.tripplanner.data.TripManager;
import krisapps.tripplanner.data.trip.Trip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DocumentGenerator {

    /**
     * Generates a trip plan PDF document for the supplied trip instance.
     *
     * @param trip   The trip to generate the plan document for.
     * @param output The location where the file will be generated.
     * @return The file pointing to the generated document.
     */
    public static File generateTripPlan(Trip trip, Path output) {
        if (trip == null) {
            throw new IllegalArgumentException("Cannot generate trip plan for null trip");
        }
        if (output == null) {
            throw new IllegalArgumentException("Cannot generate trip plan without an output directory");
        }
        if (!output.toFile().isDirectory()) {
            throw new IllegalArgumentException("The output path must point to a directory");
        }

        String htmlTemplate = "";
        try {
            htmlTemplate = new String(
                    Files.readAllBytes(Paths.get("trip_plan_template.html"))
            );
        } catch (IOException e) {
            TripManager.log("--------------------------------------");
            TripManager.log("Error reading trip_plan_template.html");
            TripManager.log(e.getMessage());
            TripManager.log("--------------------------------------");
        }


        try (OutputStream os = new FileOutputStream("%s-trip-plan.pdf".formatted(trip.getTripName().toLowerCase().replaceAll(" ", ""))) {
        }) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(htmlTemplate, null);
            builder.toStream(os);
            builder.run();
            return null; /* Replace with actual result */
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

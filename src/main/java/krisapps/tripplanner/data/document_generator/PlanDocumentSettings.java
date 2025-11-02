package krisapps.tripplanner.data.document_generator;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

public class PlanDocumentSettings {
    private String outputFolder;
    private boolean breakPageForDays = false;
    private HashMap<PlanSection, Boolean> includeSections;
    private boolean modified = false;

    public PlanDocumentSettings(String outputFolder, boolean breakPageForDays) {
        this.outputFolder = outputFolder;
        this.breakPageForDays = breakPageForDays;
        this.includeSections = new HashMap<>();
        for (PlanSection section : PlanSection.values()) {
            includeSections.put(section, true);
        }
    }

    public PlanDocumentSettings(String outputFolder, boolean breakPageForDays, HashMap<PlanSection, Boolean> includeSections) {
        this.outputFolder = outputFolder;
        this.breakPageForDays = breakPageForDays;
        this.includeSections = includeSections;
    }

    public PlanDocumentSettings() {
        this.outputFolder = System.getProperty("user.home") + File.separator + "Desktop";
        this.breakPageForDays = false;
        this.includeSections = new HashMap<>();
        for (PlanSection section : PlanSection.values()) {
            this.includeSections.put(section, true);
        }
    }

    public Path getOutputFolder() {
        return Path.of(outputFolder);
    }

    public void setOutputFolder(Path outputFolder) {
        this.outputFolder = outputFolder.toString();
        this.modified = true;
    }

    public boolean shouldBreakPageForEachDay() {
        return this.breakPageForDays;
    }

    public void setBreakPageForDays(boolean breakPageForDays) {
        this.breakPageForDays = breakPageForDays;
        this.modified = true;
    }

    public HashMap<PlanSection, Boolean> getIncludeSections() {
        return includeSections;
    }

    public void setIncludeSections(HashMap<PlanSection, Boolean> includeSections) {
        this.includeSections = includeSections;
    }

    public boolean hasBeenModified() {
        return modified;
    }

    public void resetModifiedFlag() {
        this.modified = false;
    }

    public PlanDocumentSettings copy() {
        return new PlanDocumentSettings(outputFolder, breakPageForDays, includeSections);
    }
}

package krisapps.tripplanner.data;

import krisapps.tripplanner.data.document_generator.PlanDocumentSettings;

public class ProgramSettings {

    private boolean modified = false;

    private String currencySymbol;
    private boolean currencySymbolPrefix = false;
    private boolean openLastTrip = false;
    private PlanDocumentSettings documentGeneratorSettings = new PlanDocumentSettings();

    public ProgramSettings() {
        this.modified = false;
        this.currencySymbol = "€";
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
        this.modified = true;
    }

    public void resetModifiedFlag() {
        this.documentGeneratorSettings.resetModifiedFlag();
        this.modified = false;
    }

    public boolean haveBeenModified() {
        this.modified = modified || documentGeneratorSettings.hasBeenModified();
        return modified;
    }

    public boolean currencySymbolPrefixed() {
        return currencySymbolPrefix;
    }

    public void setCurrencySymbolPrefixed(boolean prefixed) {
        this.currencySymbolPrefix = prefixed;
        this.modified = true;
    }

    public boolean shouldOpenLastTrip() {
        return openLastTrip;
    }

    public void setOpenLastTrip(boolean openLastTrip) {
        this.openLastTrip = openLastTrip;
        this.modified = true;
    }

    public PlanDocumentSettings getDocumentGeneratorSettings() {
        return documentGeneratorSettings;
    }

    public void setDocumentGeneratorSettings(PlanDocumentSettings settings) {
        this.documentGeneratorSettings = settings;
        this.modified = true;
    }
}

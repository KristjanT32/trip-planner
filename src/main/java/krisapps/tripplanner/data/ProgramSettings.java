package krisapps.tripplanner.data;

import java.io.File;
import java.nio.file.Path;

public class ProgramSettings {

    private boolean modified = false;

    private String currencySymbol;
    private boolean currencySymbolPrefix = false;
    private String documentGeneratorOutputFolder;
    private boolean openLastTrip = false;
    private boolean breakPageForDays = false;

    public ProgramSettings() {
        this.modified = false;
        this.currencySymbol = "€";
        this.documentGeneratorOutputFolder = System.getProperty("user.home") + File.separator + "Desktop";
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
        this.modified = true;
    }

    public void resetModifiedFlag() {
        this.modified = false;
    }

    public boolean haveBeenModified() {
        return modified;
    }

    public boolean currencySymbolPrefixed() {
        return currencySymbolPrefix;
    }

    public void setCurrencySymbolPrefixed(boolean prefixed) {
        this.currencySymbolPrefix = prefixed;
        this.modified = true;
    }

    public Path getDocumentGeneratorOutputFolder() {
        return Path.of(documentGeneratorOutputFolder);
    }

    public void setDocumentGeneratorOutputFolder(Path documentGeneratorOutputFolder) {
        this.documentGeneratorOutputFolder = documentGeneratorOutputFolder.toString();
        this.modified = true;
    }

    public boolean shouldOpenLastTrip() {
        return openLastTrip;
    }

    public void setOpenLastTrip(boolean openLastTrip) {
        this.openLastTrip = openLastTrip;
        this.modified = true;
    }

    public boolean shouldBreakPageForEachDay() {
        return breakPageForDays;
    }

    public void setBreakPageForDays(boolean breakPageForDays) {
        this.breakPageForDays = breakPageForDays;
        this.modified = true;
    }
}

package krisapps.tripplanner.data;

public enum CountdownFormat {
    DEFAULT("00d:00h:00m:00s left"),
    HMS("00h:00m:00s left"),
    DAYS("00 days left"),
    HOURS("00 hours left"),
    ;

    private String format;

    CountdownFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public static CountdownFormat of(String format) {
        for (CountdownFormat countdownFormat : CountdownFormat.values()) {
            if (countdownFormat.getFormat().equals(format)) {
                return countdownFormat;
            }
        }
        return DEFAULT;
    }
}

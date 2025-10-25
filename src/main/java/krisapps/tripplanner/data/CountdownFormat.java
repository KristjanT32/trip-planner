package krisapps.tripplanner.data;

public enum CountdownFormat {
    DEFAULT("00d:00h:00m:00s left", "dd'd' : HH'h' : mm'm' : ss's' left"),
    HMS("00h:00m:00s left", "HH'h' : mm'm' : ss's' 'left'"),
    DAYS("00 days left", "dd 'days left'"),
    HOURS("00 hours left", "HH 'hours left'"),
    ;

    private String preview;
    private String format;

    CountdownFormat(String preview, String format) {
        this.preview = preview;
        this.format = format;
    }

    public String getPreview() {
        return preview;
    }

    public String getFormat() {
        return format;
    }

    public static CountdownFormat of(String format) {
        for (CountdownFormat countdownFormat : CountdownFormat.values()) {
            if (countdownFormat.getPreview().equals(format)) {
                return countdownFormat;
            }
        }
        return DEFAULT;
    }
}

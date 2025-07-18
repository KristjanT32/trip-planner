package krisapps.tripplanner.data;

import java.util.*;

public class Data {

    private HashMap<String, Object> values;

    public Data() {
        this.values = new HashMap<>();
    }

    public HashMap<String, Object> getSavedValues() {
        return this.values;
    }
}

package krisapps.tripplanner.data.document_generator;

public class PlanSectionState {
    private PlanSection section;
    private boolean include = false;

    public PlanSectionState(PlanSection section, boolean include) {
        this.section = section;
        this.include = include;
    }

    public PlanSection getSection() {
        return section;
    }

    public void setIncluded(boolean include) {
        this.include = include;
    }

    public boolean isIncluded() {
        return include;
    }
}

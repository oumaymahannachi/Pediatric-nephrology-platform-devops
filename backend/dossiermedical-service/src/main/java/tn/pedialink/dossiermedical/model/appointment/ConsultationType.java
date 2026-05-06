package tn.pedialink.dossiermedical.model.appointment;

public enum ConsultationType {
    STANDARD(30, "Standard consultation"),
    URGENT(45, "Urgent consultation"),
    FOLLOW_UP(20, "Follow-up consultation"),
    EMERGENCY(60, "Emergency consultation"),
    FIRST_VISIT(45, "First visit consultation");

    private final int durationMinutes;
    private final String description;

    ConsultationType(int durationMinutes, String description) {
        this.durationMinutes = durationMinutes;
        this.description = description;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getDescription() {
        return description;
    }
}

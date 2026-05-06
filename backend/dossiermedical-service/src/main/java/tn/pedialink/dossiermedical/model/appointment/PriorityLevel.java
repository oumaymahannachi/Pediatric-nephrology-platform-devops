package tn.pedialink.dossiermedical.model.appointment;

public enum PriorityLevel {
    CRITICAL(5, "Critical - Immediate attention required", "red"),
    URGENT(4, "Urgent - Within 24-48 hours", "orange"),
    HIGH(3, "High - Within 1 week", "yellow"),
    NORMAL(2, "Normal - Within 2 weeks", "green"),
    LOW(1, "Low - Routine follow-up", "blue");

    private final int score;
    private final String description;
    private final String colorCode;

    PriorityLevel(int score, String description, String colorCode) {
        this.score = score;
        this.description = description;
        this.colorCode = colorCode;
    }

    public int getScore() {
        return score;
    }

    public String getDescription() {
        return description;
    }

    public String getColorCode() {
        return colorCode;
    }
}

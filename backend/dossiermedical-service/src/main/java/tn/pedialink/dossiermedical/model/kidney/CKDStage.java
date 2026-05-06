package tn.pedialink.dossiermedical.model.kidney;

public enum CKDStage {
    STAGE_1("Stage 1", "Normal or high GFR", 90, Double.MAX_VALUE, "green"),
    STAGE_2("Stage 2", "Mild CKD", 60, 89, "yellow"),
    STAGE_3A("Stage 3a", "Mild to moderate CKD", 45, 59, "orange"),
    STAGE_3B("Stage 3b", "Moderate to severe CKD", 30, 44, "orange"),
    STAGE_4("Stage 4", "Severe CKD", 15, 29, "red"),
    STAGE_5("Stage 5", "Kidney failure (ESRD)", 0, 14, "darkred");

    private final String name;
    private final String description;
    private final double minGFR;
    private final double maxGFR;
    private final String colorCode;

    CKDStage(String name, String description, double minGFR, double maxGFR, String colorCode) {
        this.name = name;
        this.description = description;
        this.minGFR = minGFR;
        this.maxGFR = maxGFR;
        this.colorCode = colorCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getMinGFR() {
        return minGFR;
    }

    public double getMaxGFR() {
        return maxGFR;
    }

    public String getColorCode() {
        return colorCode;
    }

    public static CKDStage fromGFR(double gfr) {
        for (CKDStage stage : values()) {
            if (gfr >= stage.minGFR && gfr <= stage.maxGFR) {
                return stage;
            }
        }
        return STAGE_5; // Default to worst case
    }
}

package tn.pedialink.labresults.entity;

public enum CKDStage {
    STAGE_1,    // eGFR >= 90 (Normal)
    STAGE_2,    // eGFR 60-89 (Légèrement diminué)
    STAGE_3A,   // eGFR 45-59 (Modérément diminué)
    STAGE_3B,   // eGFR 30-44 (Modérément à sévèrement diminué)
    STAGE_4,    // eGFR 15-29 (Sévèrement diminué)
    STAGE_5,    // eGFR < 15 (Insuffisance rénale terminale)
    UNKNOWN     // Impossible à calculer
}

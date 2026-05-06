package tn.pedialink.dossiermedical.model.consultation;

public enum StatutConsultation {
    EN_ATTENTE,    // Demande créée par parent, en attente de réponse médecin
    ACCEPTEE,      // Acceptée par médecin
    REFUSEE,       // Refusée par médecin (avec date proposée)
    TERMINEE,      // Consultation effectuée
    ANNULEE        // Annulée
}

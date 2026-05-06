package tn.pedialink.prescription.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.prescription.model.Prescription;

import java.util.*;

@Slf4j
@Service
public class InteractionMedicamenteuseService {

    private static final Map<String, List<String>> INTERACTIONS_DB = new HashMap<>();

    static {
        INTERACTIONS_DB.put("WARFARINE", Arrays.asList("ASPIRINE", "IBUPROFENE", "AMIODARONE"));
        INTERACTIONS_DB.put("DIGOXINE", Arrays.asList("AMIODARONE", "VERAPAMIL", "ERYTHROMYCINE"));
        INTERACTIONS_DB.put("METFORMINE", Arrays.asList("CONTRASTE_IODE", "ALCOOL"));
        INTERACTIONS_DB.put("IEC", Arrays.asList("SPIRONOLACTONE", "LITHIUM", "AINS"));
        INTERACTIONS_DB.put("STATINE", Arrays.asList("CYCHOSPORINE", "GEMFIBROZIL", "AZOLE"));
    }

    public List<String> verifierInteractions(List<String> dcis) {
        List<String> alertes = new ArrayList<>();
        Set<String> dcisSet = new HashSet<>();

        for (String dci : dcis) {
            if (dci != null) {
                dcisSet.add(dci.toUpperCase());
            }
        }

        for (String dci : dcis) {
            if (dci == null) continue;
            
            String dciUpper = dci.toUpperCase();
            List<String> interactions = INTERACTIONS_DB.get(dciUpper);

            if (interactions != null) {
                for (String interactant : interactions) {
                    if (dcisSet.contains(interactant)) {
                        String alerte = String.format(
                                "INTERACTION MAJEURE: %s ↔ %s - Surveillance clinique requise",
                                dci, interactant
                        );
                        alertes.add(alerte);
                        log.warn("Interaction détectée: {} avec {}", dciUpper, interactant);
                    }
                }
            }
        }

        return alertes;
    }

    public List<String> detecterInteractions(List<Prescription.Medicament> medicaments) {
        List<String> alertes = new ArrayList<>();
        Set<String> dcis = new HashSet<>();

        for (Prescription.Medicament med : medicaments) {
            dcis.add(med.getDci().toUpperCase());
        }

        for (Prescription.Medicament med : medicaments) {
            String dci = med.getDci().toUpperCase();
            List<String> interactions = INTERACTIONS_DB.get(dci);

            if (interactions != null) {
                for (String interactant : interactions) {
                    if (dcis.contains(interactant)) {
                        String alerte = String.format(
                                "INTERACTION MAJEURE: %s ↔ %s - Surveillance clinique requise",
                                med.getDci(), interactant
                        );
                        alertes.add(alerte);
                        log.warn("Interaction détectée: {} avec {}", dci, interactant);
                    }
                }
            }
        }

        detecterDoublonsTherapeutiques(medicaments, alertes);

        return alertes;
    }

    public Prescription.Posologie calculerPosologiePediatrique(
            String dci,
            Double poidsKg,
            Double surfaceCorporelleM2,
            Integer age) {

        Map<String, Double> DOSES_PEDIATRIQUES = new HashMap<>();
        DOSES_PEDIATRIQUES.put("PARACETAMOL", 15.0);
        DOSES_PEDIATRIQUES.put("IBUPROFENE", 10.0);
        DOSES_PEDIATRIQUES.put("AMOXICILLINE", 50.0);
        DOSES_PEDIATRIQUES.put("AUGMENTIN", 40.0);

        Double doseParKg = DOSES_PEDIATRIQUES.get(dci.toUpperCase());

        if (doseParKg != null && poidsKg != null) {
            Double doseTotale = doseParKg * poidsKg;
            String calculDetails = String.format("%.1f mg/kg x %.1f kg = %.1f mg",
                    doseParKg, poidsKg, doseTotale);

            return Prescription.Posologie.builder()
                    .isPediatrique(true)
                    .poidsPatientKg(poidsKg)
                    .surfaceCorporelleM2(surfaceCorporelleM2)
                    .doseParKg(doseParKg)
                    .quantite(doseTotale)
                    .unite("mg")
                    .calculDoseDetails(calculDetails)
                    .build();
        }

        return null;
    }

    private void detecterDoublonsTherapeutiques(
            List<Prescription.Medicament> medicaments,
            List<String> alertes) {

        for (int i = 0; i < medicaments.size(); i++) {
            for (int j = i + 1; j < medicaments.size(); j++) {
                if (sontMemeClasse(medicaments.get(i).getDci(), medicaments.get(j).getDci())) {
                    alertes.add(String.format("DOUBLON THERAPEUTIQUE: %s et %s",
                            medicaments.get(i).getDci(), medicaments.get(j).getDci()));
                }
            }
        }
    }

    private boolean sontMemeClasse(String dci1, String dci2) {
        String[] betaBlockers = {"PROPRANOLOL", "METOPROLOL", "BISOPROLOL"};
        String[] aceInhibitors = {"ENALAPRIL", "PERINDOPRIL", "RAMIPRIL"};

        return sontDansMemeGroupe(dci1, dci2, betaBlockers) ||
                sontDansMemeGroupe(dci1, dci2, aceInhibitors);
    }

    private boolean sontDansMemeGroupe(String dci1, String dci2, String[] groupe) {
        boolean d1In = Arrays.asList(groupe).contains(dci1.toUpperCase());
        boolean d2In = Arrays.asList(groupe).contains(dci2.toUpperCase());
        return d1In && d2In;
    }
}
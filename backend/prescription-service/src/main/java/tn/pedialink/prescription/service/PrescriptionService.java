package tn.pedialink.prescription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.pedialink.prescription.dto.prescription.PrescriptionCreateRequest;
import tn.pedialink.prescription.dto.prescription.PrescriptionResponse;
import tn.pedialink.prescription.dto.prescription.PrescriptionUpdateRequest;
import tn.pedialink.prescription.exception.BusinessException;
import tn.pedialink.prescription.exception.ResourceNotFoundException;
import tn.pedialink.prescription.exception.UnauthorizedException;
import tn.pedialink.prescription.model.Prescription;
import tn.pedialink.prescription.repository.PrescriptionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final InteractionMedicamenteuseService interactionService;
    private final MongoTemplate mongoTemplate;


    public PrescriptionResponse creerPrescription(String medecinId, PrescriptionCreateRequest request) {
        log.info("Création d'une prescription pour le patient {} par le médecin {}",
                request.getPatientId(), medecinId);

        // Vérifier les interactions médicamenteuses
        List<String> alertesInteractions = verifierInteractions(request.getMedicaments());

        // Calculer la date d'expiration
        LocalDate dateExpiration = request.getDatePrescription()
                .plusDays(request.getDureeValiditeJours());

        // Construire la prescription
        Prescription prescription = Prescription.builder()
                .patientId(request.getPatientId())
                .medecinId(medecinId)
                .datePrescription(request.getDatePrescription())
                .dateExpiration(dateExpiration)
                .dureeValiditeJours(request.getDureeValiditeJours())
                .diagnostic(request.getDiagnostic())
                .medicaments(convertirMedicaments(request.getMedicaments()))
                .notes(request.getNotes())
                .statut(Prescription.StatutPrescription.ACTIVE)
                .renouvelable(request.getRenouvelable() != null ? request.getRenouvelable() : false)
                .nombreRenouvellementsRestants(request.getNombreRenouvellementsAutorises())
                .nombreRenouvellementsEffectues(0)
                .build();

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("Prescription créée avec ID: {}", saved.getId());

        return convertirEnResponse(saved, alertesInteractions);
    }


    public PrescriptionResponse getPrescription(String id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription non trouvée: " + id));

        return convertirEnResponse(prescription, new ArrayList<>());
    }


    public List<PrescriptionResponse> getPrescriptionsPatient(String patientId) {
        return prescriptionRepository.findByPatientIdOrderByDatePrescriptionDesc(patientId)
                .stream()
                .map(p -> convertirEnResponse(p, new ArrayList<>()))
                .collect(Collectors.toList());
    }


    public PrescriptionResponse renouvelerPrescription(String prescriptionId, String medecinId) {
        log.info("Renouvellement de la prescription {} par le médecin {}", prescriptionId, medecinId);

        Prescription ancienne = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription non trouvée: " + prescriptionId));

        if (!ancienne.getRenouvelable()) {
            throw new BusinessException("Cette prescription n'est pas renouvelable");
        }

        if (ancienne.getNombreRenouvellementsRestants() != null
                && ancienne.getNombreRenouvellementsRestants() <= 0) {
            throw new BusinessException("Nombre maximum de renouvellements atteint");
        }

        // Créer la nouvelle prescription
        LocalDate nouvelleDate = LocalDate.now();
        LocalDate nouvelleExpiration = nouvelleDate.plusDays(ancienne.getDureeValiditeJours());

        Prescription nouvelle = Prescription.builder()
                .patientId(ancienne.getPatientId())
                .medecinId(medecinId)
                .datePrescription(nouvelleDate)
                .dateExpiration(nouvelleExpiration)
                .dureeValiditeJours(ancienne.getDureeValiditeJours())
                .diagnostic(ancienne.getDiagnostic())
                .medicaments(ancienne.getMedicaments())
                .notes(ancienne.getNotes() + " [Renouvellement de " + prescriptionId + "]")
                .statut(Prescription.StatutPrescription.ACTIVE)
                .renouvelable(ancienne.getRenouvelable())
                .nombreRenouvellementsRestants(
                        ancienne.getNombreRenouvellementsRestants() != null
                                ? ancienne.getNombreRenouvellementsRestants() - 1
                                : null)
                .nombreRenouvellementsEffectues(0)
                .build();

        Prescription saved = prescriptionRepository.save(nouvelle);

        // Mettre à jour l'ancienne prescription
        ancienne.setStatut(Prescription.StatutPrescription.RENOUVELEE);
        ancienne.setNombreRenouvellementsEffectues(
                ancienne.getNombreRenouvellementsEffectues() + 1);
        prescriptionRepository.save(ancienne);

        return convertirEnResponse(saved, new ArrayList<>());
    }


    public PrescriptionResponse updatePrescription(String id, PrescriptionUpdateRequest request) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription non trouvée: " + id));

        if (request.getDiagnostic() != null) {
            prescription.setDiagnostic(request.getDiagnostic());
        }
        if (request.getDureeValiditeJours() != null) {
            prescription.setDureeValiditeJours(request.getDureeValiditeJours());
            prescription.setDateExpiration(
                    prescription.getDatePrescription().plusDays(request.getDureeValiditeJours()));
        }
        if (request.getMedicaments() != null) {
            prescription.setMedicaments(convertirMedicaments(request.getMedicaments()));
        }
        if (request.getNotes() != null) {
            prescription.setNotes(request.getNotes());
        }
        if (request.getStatut() != null) {
            prescription.setStatut(request.getStatut());
        }
        if (request.getRenouvelable() != null) {
            prescription.setRenouvelable(request.getRenouvelable());
        }
        if (request.getNombreRenouvellementsRestants() != null) {
            prescription.setNombreRenouvellementsRestants(request.getNombreRenouvellementsRestants());
        }

        Prescription updated = prescriptionRepository.save(prescription);
        return convertirEnResponse(updated, new ArrayList<>());
    }


    public void annulerPrescription(String id, String raison) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription non trouvée: " + id));

        prescription.setStatut(Prescription.StatutPrescription.ANNULEE);
        prescription.setNotes(prescription.getNotes() + " [Annulée: " + raison + "]");
        prescriptionRepository.save(prescription);
    }

    // Tâche planifiée: mettre à jour les prescriptions expirées
    @Scheduled(cron = "0 0 0 * * ?") // Tous les jours à minuit
    public void mettreAJourPrescriptionsExpirees() {
        log.info("Mise à jour des prescriptions expirées...");

        LocalDate today = LocalDate.now();

        Query query = new Query(Criteria.where("statut").is("ACTIVE")
                .and("dateExpiration").lt(today));

        Update update = new Update()
                .set("statut", Prescription.StatutPrescription.EXPIREE)
                .set("updatedAt", LocalDateTime.now());

        mongoTemplate.updateMulti(query, update, Prescription.class);

        log.info("Mise à jour des prescriptions expirées terminée");
    }

    // Méthodes utilitaires privées
    private List<String> verifierInteractions(List<PrescriptionCreateRequest.MedicamentRequest> medicaments) {
        List<String> dcis = medicaments.stream()
                .map(PrescriptionCreateRequest.MedicamentRequest::getDci)
                .collect(Collectors.toList());

        return interactionService.verifierInteractions(dcis);
    }

    private List<Prescription.Medicament> convertirMedicaments(
            List<PrescriptionCreateRequest.MedicamentRequest> requests) {
        return requests.stream()
                .map(this::convertirMedicament)
                .collect(Collectors.toList());
    }

    private Prescription.Medicament convertirMedicament(PrescriptionCreateRequest.MedicamentRequest request) {
        return Prescription.Medicament.builder()
                .nomCommercial(request.getNomCommercial())
                .dci(request.getDci())
                .formePharmaceutique(request.getFormePharmaceutique())
                .dosage(request.getDosage())
                .posologie(convertirPosologie(request.getPosologie()))
                .instructionsSpeciales(request.getInstructionsSpeciales())
                .substitutable(request.getSubstitutable())
                .build();
    }

    private Prescription.Posologie convertirPosologie(PrescriptionCreateRequest.PosologieRequest request) {
        if (request == null) return null;

        String calculDetails = null;
        Double doseTotale = null;

        // Calcul pédiatrique
        if (Boolean.TRUE.equals(request.getIsPediatrique())) {
            if (request.getDoseParKg() != null && request.getPoidsPatientKg() != null) {
                doseTotale = request.getDoseParKg() * request.getPoidsPatientKg();
                calculDetails = String.format("Dose calculée: %.2f mg/kg × %.1f kg = %.2f",
                        request.getDoseParKg(), request.getPoidsPatientKg(), doseTotale);
            } else if (request.getSurfaceCorporelleM2() != null) {
                // Calcul basé sur la surface corporelle
                doseTotale = request.getDoseParKg() * request.getSurfaceCorporelleM2();
                calculDetails = String.format("Dose basée sur SC: %.3f m²",
                        request.getSurfaceCorporelleM2());
            }
        }

        return Prescription.Posologie.builder()
                .quantite(request.getQuantite())
                .unite(request.getUnite())
                .frequence(request.getFrequence())
                .momentPrise(request.getMomentPrise())
                .dureeTraitementJours(request.getDureeTraitementJours())
                .isPediatrique(request.getIsPediatrique())
                .poidsPatientKg(request.getPoidsPatientKg())
                .doseParKg(request.getDoseParKg())
                .surfaceCorporelleM2(request.getSurfaceCorporelleM2())
                .doseTotaleCalculee(doseTotale)
                .calculDoseDetails(calculDetails)
                .ajustementRenal(request.getAjustementRenal())
                .ajustementHepatique(request.getAjustementHepatique())
                .justificationAjustement(request.getJustificationAjustement())
                .build();
    }

    private PrescriptionResponse convertirEnResponse(Prescription p, List<String> alertes) {
        return PrescriptionResponse.builder()
                .id(p.getId())
                .patientId(p.getPatientId())
                .medecinId(p.getMedecinId())
                .datePrescription(p.getDatePrescription())
                .dateExpiration(p.getDateExpiration())
                .dureeValiditeJours(p.getDureeValiditeJours())
                .diagnostic(p.getDiagnostic())
                .medicaments(p.getMedicaments().stream()
                        .map(this::convertirMedicamentResponse)
                        .collect(Collectors.toList()))
                .notes(p.getNotes())
                .statut(p.getStatut())
                .renouvelable(p.getRenouvelable())
                .nombreRenouvellementsRestants(p.getNombreRenouvellementsRestants())
                .nombreRenouvellementsEffectues(p.getNombreRenouvellementsEffectues())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .alertes(alertes)
                .build();
    }

    private PrescriptionResponse.MedicamentResponse convertirMedicamentResponse(Prescription.Medicament m) {
        return PrescriptionResponse.MedicamentResponse.builder()
                .nomCommercial(m.getNomCommercial())
                .dci(m.getDci())
                .formePharmaceutique(m.getFormePharmaceutique())
                .dosage(m.getDosage())
                .posologie(convertirPosologieResponse(m.getPosologie()))
                .instructionsSpeciales(m.getInstructionsSpeciales())
                .substitutable(m.getSubstitutable())
                .interactionsDetectees(m.getInteractionsConnues())
                .build();
    }

    private PrescriptionResponse.PosologieResponse convertirPosologieResponse(Prescription.Posologie p) {
        if (p == null) return null;

        return PrescriptionResponse.PosologieResponse.builder()
                .quantite(p.getQuantite())
                .unite(p.getUnite())
                .frequence(p.getFrequence())
                .momentPrise(p.getMomentPrise())
                .dureeTraitementJours(p.getDureeTraitementJours())
                .isPediatrique(p.getIsPediatrique())
                .poidsPatientKg(p.getPoidsPatientKg())
                .doseParKg(p.getDoseParKg())
                .doseTotaleCalculee(p.getDoseTotaleCalculee())
                .calculDoseDetails(p.getCalculDoseDetails())
                .ajustementRenal(p.getAjustementRenal())
                .ajustementHepatique(p.getAjustementHepatique())
                .build();
    }

    public PrescriptionResponse modifierPrescription(String id, String medecinId, PrescriptionCreateRequest request) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription non trouvée: " + id));

        // Vérifier que c'est le même médecin
        if (!prescription.getMedecinId().equals(medecinId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à modifier cette prescription");
        }

        // Mettre à jour les champs
        prescription.setDiagnostic(request.getDiagnostic());
        prescription.setDureeValiditeJours(request.getDureeValiditeJours());
        prescription.setDateExpiration(prescription.getDatePrescription().plusDays(request.getDureeValiditeJours()));
        prescription.setNotes(request.getNotes());
        prescription.setRenouvelable(request.getRenouvelable());
        prescription.setNombreRenouvellementsRestants(request.getNombreRenouvellementsAutorises());
        prescription.setMedicaments(convertirMedicaments(request.getMedicaments()));

        Prescription updated = prescriptionRepository.save(prescription);
        return convertirEnResponse(updated, new ArrayList<>());
    }

    public void supprimerPrescription(String id, String medecinId) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription non trouvée: " + id));

        // Vérifier que c'est le même médecin
        if (!prescription.getMedecinId().equals(medecinId)) {
            throw new UnauthorizedException("Vous n'êtes pas autorisé à supprimer cette prescription");
        }

        prescriptionRepository.deleteById(id);
    }
}

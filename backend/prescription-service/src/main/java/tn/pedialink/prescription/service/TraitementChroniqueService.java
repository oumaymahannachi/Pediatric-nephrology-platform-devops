package tn.pedialink.prescription.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.pedialink.prescription.dto.traitement.TraitementCreateRequest;
import tn.pedialink.prescription.dto.traitement.TraitementResponse;
import tn.pedialink.prescription.dto.traitement.TraitementUpdateRequest;
import tn.pedialink.prescription.exception.BusinessException;
import tn.pedialink.prescription.exception.ResourceNotFoundException;
import tn.pedialink.prescription.model.Prescription;
import tn.pedialink.prescription.model.TraitementChronique;
import tn.pedialink.prescription.repository.TraitementChroniqueRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TraitementChroniqueService {

    private final TraitementChroniqueRepository traitementRepository;
    private final PrescriptionService prescriptionService;


    public TraitementResponse creerTraitement(String medecinId, TraitementCreateRequest request) {
        log.info("Création d'un traitement chronique pour le patient {} par le médecin {}",
                request.getPatientId(), medecinId);

        // Calculer la date de prochain renouvellement
        LocalDate dateProchainRenouvellement = request.getDateDebut()
                .plusDays(request.getDureeEntreRenouvellementsJours());

        TraitementChronique traitement = TraitementChronique.builder()
                .patientId(request.getPatientId())
                .medecinPrescripteurId(medecinId)
                .nomTraitement(request.getNomTraitement())
                .pathologie(request.getPathologie())
                .dateDebut(request.getDateDebut())
                .dateFinPrevue(request.getDateFinPrevue())
                .actif(true)
                .medicaments(convertirMedicaments(request.getMedicaments()))
                .dureeEntreRenouvellementsJours(request.getDureeEntreRenouvellementsJours())
                .dateProchainRenouvellement(dateProchainRenouvellement)
                .bilansProgrammes(convertirBilans(request.getBilansProgrammes()))
                .notesEvolution(request.getNotesEvolution())
                .build();

        TraitementChronique saved = traitementRepository.save(traitement);
        log.info("Traitement chronique créé avec ID: {}", saved.getId());

        return convertirEnResponse(saved);
    }


    public List<TraitementResponse> getTraitementsActifsPatient(String patientId) {
        return traitementRepository.findByPatientIdAndActifTrue(patientId)
                .stream()
                .map(this::convertirEnResponse)
                .collect(Collectors.toList());
    }


    public TraitementResponse enregistrerRenouvellement(String traitementId, String prescriptionId,
                                                        String pharmacienId) {
        log.info("Enregistrement du renouvellement pour le traitement {}", traitementId);

        TraitementChronique traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new ResourceNotFoundException("Traitement non trouvé: " + traitementId));

        if (!traitement.getActif()) {
            throw new BusinessException("Ce traitement n'est plus actif");
        }

        LocalDate dateRenouvellement = LocalDate.now();

        // Créer l'entrée d'historique
        TraitementChronique.Renouvellement renouvellement = TraitementChronique.Renouvellement.builder()
                .dateRenouvellement(dateRenouvellement)
                .prescriptionId(prescriptionId)
                .medecinId(traitement.getMedecinPrescripteurId())
                .pharmacienId(pharmacienId)
                .nombreUnitesDelivrees(calculerUnitesDelivrees(traitement))
                .avecConsultation(false) // À déterminer selon la logique métier
                .build();

        traitement.getHistoriqueRenouvellements().add(renouvellement);
        traitement.setDateDernierRenouvellement(dateRenouvellement);
        traitement.setDateProchainRenouvellement(
                dateRenouvellement.plusDays(traitement.getDureeEntreRenouvellementsJours()));

        // Vérifier si le traitement doit se terminer
        if (traitement.getDateFinPrevue() != null &&
                traitement.getDateProchainRenouvellement().isAfter(traitement.getDateFinPrevue())) {
            traitement.setActif(false);
            traitement.setDateFinEffective(traitement.getDateFinPrevue());
        }

        TraitementChronique updated = traitementRepository.save(traitement);
        return convertirEnResponse(updated);
    }


    public void enregistrerObservance(String traitementId, LocalDate date, Boolean pris,
                                      Integer heurePrise, String commentaire) {
        TraitementChronique traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new ResourceNotFoundException("Traitement non trouvé: " + traitementId));

        TraitementChronique.Observance observance = TraitementChronique.Observance.builder()
                .date(date)
                .pris(pris)
                .heurePrise(heurePrise)
                .commentaire(commentaire)
                .oublie(!pris)
                .prisHorsHoraire(heurePrise != null && estHorsHoraire(heurePrise))
                .build();

        traitement.getHistoriqueObservance().add(observance);
        traitementRepository.save(traitement);

        log.info("Observance enregistrée pour le traitement {} à la date {}", traitementId, date);
    }


    public void marquerBilanRealise(String traitementId, String typeBilan,
                                    LocalDate dateRealisation, String resultat) {
        TraitementChronique traitement = traitementRepository.findById(traitementId)
                .orElseThrow(() -> new ResourceNotFoundException("Traitement non trouvé: " + traitementId));

        traitement.getBilansProgrammes().stream()
                .filter(b -> b.getTypeBilan().equals(typeBilan) && !b.getRealise())
                .findFirst()
                .ifPresent(bilan -> {
                    bilan.setRealise(true);
                    bilan.setDateRealisation(dateRealisation);
                    bilan.setResultat(resultat);
                });

        traitementRepository.save(traitement);
        log.info("Bilan {} marqué comme réalisé pour le traitement {}", typeBilan, traitementId);
    }


    public TraitementResponse updateTraitement(String id, TraitementUpdateRequest request) {
        TraitementChronique traitement = traitementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Traitement non trouvé: " + id));

        if (request.getNomTraitement() != null) {
            traitement.setNomTraitement(request.getNomTraitement());
        }
        if (request.getPathologie() != null) {
            traitement.setPathologie(request.getPathologie());
        }
        if (request.getDateFinPrevue() != null) {
            traitement.setDateFinPrevue(request.getDateFinPrevue());
        }
        if (request.getActif() != null) {
            traitement.setActif(request.getActif());
            if (!request.getActif()) {
                traitement.setDateFinEffective(LocalDate.now());
            }
        }
        if (request.getDureeEntreRenouvellementsJours() != null) {
            traitement.setDureeEntreRenouvellementsJours(request.getDureeEntreRenouvellementsJours());
            // Recalculer la date de prochain renouvellement
            if (traitement.getDateDernierRenouvellement() != null) {
                traitement.setDateProchainRenouvellement(
                        traitement.getDateDernierRenouvellement()
                                .plusDays(request.getDureeEntreRenouvellementsJours()));
            }
        }
        if (request.getMedicaments() != null) {
            traitement.setMedicaments(convertirMedicaments(request.getMedicaments()));
        }
        if (request.getNotesEvolution() != null) {
            traitement.setNotesEvolution(request.getNotesEvolution());
        }

        TraitementChronique updated = traitementRepository.save(traitement);
        return convertirEnResponse(updated);
    }


    public void terminerTraitement(String id, String raison) {
        TraitementChronique traitement = traitementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Traitement non trouvé: " + id));

        traitement.setActif(false);
        traitement.setDateFinEffective(LocalDate.now());
        traitement.setNotesEvolution(
                (traitement.getNotesEvolution() != null ? traitement.getNotesEvolution() + " " : "")
                        + "[Terminé: " + raison + "]");

        traitementRepository.save(traitement);
        log.info("Traitement {} terminé: {}", id, raison);
    }

    // Tâches planifiées
    @Scheduled(cron = "0 0 6 * * ?") // Tous les jours à 6h du matin
    public void notifierRenouvellementsProches() {
        log.info("Vérification des renouvellements proches...");

        LocalDate today = LocalDate.now();
        LocalDate dans3Jours = today.plusDays(3);

        List<TraitementChronique> traitements = traitementRepository
                .findNeedingRenewalBetween(today, dans3Jours);

        // Logique de notification à implémenter (email, push, etc.)
        traitements.forEach(t -> {
            log.info("Renouvellement urgent pour le traitement {} du patient {}",
                    t.getId(), t.getPatientId());
            // TODO: Envoyer notification au patient/pharmacien
        });
    }

    @Scheduled(cron = "0 0 7 * * ?") // Tous les jours à 7h du matin
    public void notifierBilansEnRetard() {
        log.info("Vérification des bilans en retard...");

        LocalDate today = LocalDate.now();
        LocalDate ilYa7Jours = today.minusDays(7);

        List<TraitementChronique> traitements = traitementRepository
                .findUpcomingBilans(ilYa7Jours, today);

        traitements.forEach(t -> {
            t.getBilansProgrammes().stream()
                    .filter(b -> !b.getRealise() && b.getDatePrevue().isBefore(today))
                    .forEach(b -> {
                        log.info("Bilan {} en retard pour le traitement {}",
                                b.getTypeBilan(), t.getId());
                        // TODO: Envoyer alerte au médecin
                    });
        });
    }

    // Méthodes utilitaires privées
    private boolean estHorsHoraire(Integer heurePrise) {
        // Considérer comme hors horaire si pris avant 6h ou après 22h
        return heurePrise < 6 || heurePrise > 22;
    }

    private Integer calculerUnitesDelivrees(TraitementChronique traitement) {
        // Logique de calcul basée sur la posologie et la durée
        return traitement.getDureeEntreRenouvellementsJours(); // Simplifié
    }

    private List<TraitementChronique.MedicamentChronique> convertirMedicaments(
            List<TraitementCreateRequest.MedicamentChroniqueRequest> requests) {
        if (requests == null) return new ArrayList<>();

        return requests.stream()
                .map(m -> TraitementChronique.MedicamentChronique.builder()
                        .nomCommercial(m.getNomCommercial())
                        .dci(m.getDci())
                        .dosage(m.getDosage())
                        .posologie(convertirPosologie(m.getPosologie()))
                        .medicamentPrincipal(m.getMedicamentPrincipal())
                        .build())
                .collect(Collectors.toList());
    }

    private Prescription.Posologie convertirPosologie(TraitementCreateRequest.PosologieRequest request) {
        if (request == null) return null;

        return Prescription.Posologie.builder()
                .quantite(request.getQuantite())
                .unite(request.getUnite())
                .frequence(request.getFrequence())
                .momentPrise(request.getMomentPrise())
                .build();
    }

    private List<TraitementChronique.Bilan> convertirBilans(
            List<TraitementCreateRequest.BilanRequest> requests) {
        if (requests == null) return new ArrayList<>();

        return requests.stream()
                .map(b -> TraitementChronique.Bilan.builder()
                        .typeBilan(b.getTypeBilan())
                        .description(b.getDescription())
                        .datePrevue(b.getDatePrevue())
                        .realise(false)
                        .alerteSiAnomalie(b.getAlerteSiAnomalie())
                        .build())
                .collect(Collectors.toList());
    }

    private TraitementResponse convertirEnResponse(TraitementChronique t) {
        long joursAvantRenouvellement = t.getDateProchainRenouvellement() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), t.getDateProchainRenouvellement())
                : 999;

        boolean renouvellementUrgent = joursAvantRenouvellement <= 3 && joursAvantRenouvellement >= 0;

        return TraitementResponse.builder()
                .id(t.getId())
                .patientId(t.getPatientId())
                .medecinPrescripteurId(t.getMedecinPrescripteurId())
                .nomTraitement(t.getNomTraitement())
                .pathologie(t.getPathologie())
                .dateDebut(t.getDateDebut())
                .dateFinPrevue(t.getDateFinPrevue())
                .dateFinEffective(t.getDateFinEffective())
                .actif(t.getActif())
                .medicaments(t.getMedicaments().stream()
                        .map(this::convertirMedicamentResponse)
                        .collect(Collectors.toList()))
                .dureeEntreRenouvellementsJours(t.getDureeEntreRenouvellementsJours())
                .dateDernierRenouvellement(t.getDateDernierRenouvellement())
                .dateProchainRenouvellement(t.getDateProchainRenouvellement())
                .historiqueRenouvellements(t.getHistoriqueRenouvellements().stream()
                        .map(this::convertirRenouvellementResponse)
                        .collect(Collectors.toList()))
                .bilansProgrammes(t.getBilansProgrammes().stream()
                        .map(this::convertirBilanResponse)
                        .collect(Collectors.toList()))
                .notesEvolution(t.getNotesEvolution())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .joursAvantRenouvellement((int) joursAvantRenouvellement)
                .renouvellementUrgent(renouvellementUrgent)
                .build();
    }

    private TraitementResponse.MedicamentChroniqueResponse convertirMedicamentResponse(
            TraitementChronique.MedicamentChronique m) {
        String posologieStr = m.getPosologie() != null
                ? String.format("%.1f %s %s", m.getPosologie().getQuantite(),
                m.getPosologie().getUnite(), m.getPosologie().getFrequence())
                : "";

        return TraitementResponse.MedicamentChroniqueResponse.builder()
                .nomCommercial(m.getNomCommercial())
                .dci(m.getDci())
                .dosage(m.getDosage())
                .posologie(posologieStr)
                .medicamentPrincipal(m.getMedicamentPrincipal())
                .build();
    }

    private TraitementResponse.RenouvellementResponse convertirRenouvellementResponse(
            TraitementChronique.Renouvellement r) {
        return TraitementResponse.RenouvellementResponse.builder()
                .dateRenouvellement(r.getDateRenouvellement())
                .prescriptionId(r.getPrescriptionId())
                .medecinId(r.getMedecinId())
                .nombreUnitesDelivrees(r.getNombreUnitesDelivrees())
                .avecConsultation(r.getAvecConsultation())
                .build();
    }

    private TraitementResponse.BilanResponse convertirBilanResponse(TraitementChronique.Bilan b) {
        boolean enRetard = !b.getRealise() && b.getDatePrevue().isBefore(LocalDate.now());

        return TraitementResponse.BilanResponse.builder()
                .typeBilan(b.getTypeBilan())
                .description(b.getDescription())
                .datePrevue(b.getDatePrevue())
                .dateRealisation(b.getDateRealisation())
                .realise(b.getRealise())
                .enRetard(enRetard)
                .build();
    }
}

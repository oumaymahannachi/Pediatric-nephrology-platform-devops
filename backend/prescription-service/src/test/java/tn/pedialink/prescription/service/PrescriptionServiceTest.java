package tn.pedialink.prescription.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.pedialink.prescription.dto.prescription.PrescriptionCreateRequest;
import tn.pedialink.prescription.dto.prescription.PrescriptionResponse;
import tn.pedialink.prescription.exception.BusinessException;
import tn.pedialink.prescription.exception.ResourceNotFoundException;
import tn.pedialink.prescription.exception.UnauthorizedException;
import tn.pedialink.prescription.model.Prescription;
import tn.pedialink.prescription.repository.PrescriptionRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du service de prescriptions")
class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private InteractionMedicamenteuseService interactionService;

    @Mock
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Prescription samplePrescription;
    private PrescriptionCreateRequest sampleRequest;

    @BeforeEach
    void setUp() {
        samplePrescription = new Prescription();
        samplePrescription.setId("presc-001");
        samplePrescription.setPatientId("patient-001");
        samplePrescription.setMedecinId("doctor-001");
        samplePrescription.setDiagnostic("Syndrome néphrotique");
        samplePrescription.setDatePrescription(LocalDate.now());
        samplePrescription.setDateExpiration(LocalDate.now().plusDays(30));
        samplePrescription.setDureeValiditeJours(30);
        samplePrescription.setStatut(Prescription.StatutPrescription.ACTIVE);
        samplePrescription.setRenouvelable(true);
        samplePrescription.setNombreRenouvellementsRestants(2);
        samplePrescription.setNombreRenouvellementsEffectues(0);
        samplePrescription.setMedicaments(List.of());

        sampleRequest = new PrescriptionCreateRequest();
        sampleRequest.setPatientId("patient-001");
        sampleRequest.setDiagnostic("Syndrome néphrotique");
        sampleRequest.setDatePrescription(LocalDate.now());
        sampleRequest.setDureeValiditeJours(30);
        sampleRequest.setMedicaments(List.of());
        sampleRequest.setRenouvelable(true);
        sampleRequest.setNombreRenouvellementsAutorises(2);
    }

    // ===== Tests création =====

    @Test
    @DisplayName("Création d'une prescription avec statut ACTIVE")
    void creerPrescription_validRequest_createsWithActiveStatus() {
        when(interactionService.verifierInteractions(any())).thenReturn(List.of());
        when(prescriptionRepository.save(any())).thenReturn(samplePrescription);

        PrescriptionResponse response = prescriptionService.creerPrescription("doctor-001", sampleRequest);

        assertNotNull(response);
        verify(prescriptionRepository).save(argThat(p ->
            p.getStatut() == Prescription.StatutPrescription.ACTIVE
        ));
    }

    @Test
    @DisplayName("Création calcule la date d'expiration automatiquement")
    void creerPrescription_calculatesExpirationDate() {
        when(interactionService.verifierInteractions(any())).thenReturn(List.of());
        when(prescriptionRepository.save(any())).thenAnswer(inv -> {
            Prescription p = inv.getArgument(0);
            assertNotNull(p.getDateExpiration());
            assertEquals(LocalDate.now().plusDays(30), p.getDateExpiration());
            return samplePrescription;
        });

        prescriptionService.creerPrescription("doctor-001", sampleRequest);
        verify(prescriptionRepository).save(any());
    }

    // ===== Tests récupération =====

    @Test
    @DisplayName("Récupération d'une prescription par ID")
    void getPrescription_existingId_returnsPrescription() {
        when(prescriptionRepository.findById("presc-001")).thenReturn(Optional.of(samplePrescription));

        PrescriptionResponse response = prescriptionService.getPrescription("presc-001");

        assertNotNull(response);
        assertEquals("presc-001", response.getId());
    }

    @Test
    @DisplayName("Récupération prescription inexistante lance ResourceNotFoundException")
    void getPrescription_nonExistingId_throwsException() {
        when(prescriptionRepository.findById("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> prescriptionService.getPrescription("unknown"));
    }

    @Test
    @DisplayName("Récupération des prescriptions d'un patient")
    void getPrescriptionsPatient_returnsPatientPrescriptions() {
        when(prescriptionRepository.findByPatientIdOrderByDatePrescriptionDesc("patient-001"))
            .thenReturn(Arrays.asList(samplePrescription));

        List<PrescriptionResponse> responses = prescriptionService.getPrescriptionsPatient("patient-001");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    // ===== Tests renouvellement =====

    @Test
    @DisplayName("Renouvellement d'une prescription renouvelable")
    void renouvelerPrescription_renewablePrescription_createsNewPrescription() {
        when(prescriptionRepository.findById("presc-001")).thenReturn(Optional.of(samplePrescription));
        when(prescriptionRepository.save(any())).thenReturn(samplePrescription);

        PrescriptionResponse response = prescriptionService.renouvelerPrescription("presc-001", "doctor-001");

        assertNotNull(response);
        verify(prescriptionRepository, times(2)).save(any()); // nouvelle + mise à jour ancienne
    }

    @Test
    @DisplayName("Renouvellement d'une prescription non renouvelable lance BusinessException")
    void renouvelerPrescription_nonRenewable_throwsBusinessException() {
        samplePrescription.setRenouvelable(false);
        when(prescriptionRepository.findById("presc-001")).thenReturn(Optional.of(samplePrescription));

        assertThrows(BusinessException.class,
            () -> prescriptionService.renouvelerPrescription("presc-001", "doctor-001"));
    }

    @Test
    @DisplayName("Renouvellement épuisé lance BusinessException")
    void renouvelerPrescription_noRenewalsLeft_throwsBusinessException() {
        samplePrescription.setNombreRenouvellementsRestants(0);
        when(prescriptionRepository.findById("presc-001")).thenReturn(Optional.of(samplePrescription));

        assertThrows(BusinessException.class,
            () -> prescriptionService.renouvelerPrescription("presc-001", "doctor-001"));
    }

    // ===== Tests modification =====

    @Test
    @DisplayName("Modification par le bon médecin réussit")
    void modifierPrescription_correctDoctor_updatesSuccessfully() {
        when(prescriptionRepository.findById("presc-001")).thenReturn(Optional.of(samplePrescription));
        when(prescriptionRepository.save(any())).thenReturn(samplePrescription);

        PrescriptionResponse response = prescriptionService.modifierPrescription("presc-001", "doctor-001", sampleRequest);

        assertNotNull(response);
    }

    @Test
    @DisplayName("Modification par un autre médecin lance UnauthorizedException")
    void modifierPrescription_wrongDoctor_throwsUnauthorizedException() {
        when(prescriptionRepository.findById("presc-001")).thenReturn(Optional.of(samplePrescription));

        assertThrows(UnauthorizedException.class,
            () -> prescriptionService.modifierPrescription("presc-001", "other-doctor", sampleRequest));
    }

    // ===== Tests suppression =====

    @Test
    @DisplayName("Suppression par le bon médecin réussit")
    void supprimerPrescription_correctDoctor_deletesSuccessfully() {
        when(prescriptionRepository.findById("presc-001")).thenReturn(Optional.of(samplePrescription));
        doNothing().when(prescriptionRepository).deleteById("presc-001");

        assertDoesNotThrow(() -> prescriptionService.supprimerPrescription("presc-001", "doctor-001"));
        verify(prescriptionRepository).deleteById("presc-001");
    }

    @Test
    @DisplayName("Suppression par un autre médecin lance UnauthorizedException")
    void supprimerPrescription_wrongDoctor_throwsUnauthorizedException() {
        when(prescriptionRepository.findById("presc-001")).thenReturn(Optional.of(samplePrescription));

        assertThrows(UnauthorizedException.class,
            () -> prescriptionService.supprimerPrescription("presc-001", "other-doctor"));
    }
}

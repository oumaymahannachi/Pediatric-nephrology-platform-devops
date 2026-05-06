package tn.pedialink.prescription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.pedialink.prescription.dto.prescription.PrescriptionCreateRequest;
import tn.pedialink.prescription.dto.prescription.PrescriptionResponse;
import tn.pedialink.prescription.service.PrescriptionService;
import tn.pedialink.prescription.security.JwtUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrescriptionController.class)
@DisplayName("Tests d'intégration - PrescriptionController")
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrescriptionService prescriptionService;

    @MockBean
    private JwtUtil jwtUtil;

    private PrescriptionResponse mockResponse;
    private PrescriptionCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        mockResponse = PrescriptionResponse.builder()
                .id("presc-001")
                .patientId("patient-001")
                .medecinId("doctor-001")
                .diagnostic("Syndrome néphrotique")
                .datePrescription(LocalDate.now())
                .dateExpiration(LocalDate.now().plusDays(30))
                .build();

        createRequest = new PrescriptionCreateRequest();
        createRequest.setPatientId("patient-001");
        createRequest.setDiagnostic("Syndrome néphrotique");
        createRequest.setDatePrescription(LocalDate.now());
        createRequest.setDureeValiditeJours(30);
        createRequest.setMedicaments(List.of());
    }

    // ===== Tests accès authentifié =====


    @Test
    @WithMockUser(username = "doctor-001", roles = {"DOCTOR"})
    @DisplayName("Médecin peut récupérer les prescriptions d'un patient")
    void getPrescriptionsByPatient_authenticatedDoctor_returns200() throws Exception {
        when(prescriptionService.getPrescriptionsPatient("patient-001"))
                .thenReturn(Arrays.asList(mockResponse));

        mockMvc.perform(get("/api/v1/prescriptions/patient/patient-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(username = "doctor-001", roles = {"DOCTOR"})
    @DisplayName("Médecin peut récupérer une prescription par ID")
    void getPrescriptionById_authenticatedDoctor_returns200() throws Exception {
        when(prescriptionService.getPrescription("presc-001")).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/prescriptions/presc-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("presc-001"));
    }

    @Test
    @WithMockUser(username = "doctor-001", roles = {"DOCTOR"})
    @DisplayName("Médecin peut renouveler une prescription")
    void renewPrescription_authenticatedDoctor_returns200() throws Exception {
        when(prescriptionService.renouvelerPrescription(anyString(), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/prescriptions/presc-001/renouveler")
                .with(csrf()))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "doctor-001", roles = {"DOCTOR"})
    @DisplayName("Médecin peut supprimer une prescription")
    void deletePrescription_authenticatedDoctor_returns200() throws Exception {
        doNothing().when(prescriptionService).supprimerPrescription(anyString(), anyString());

        mockMvc.perform(delete("/api/v1/prescriptions/presc-001")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    // ===== Tests sans authentification =====


    // ===== Tests validation des données =====

    @Test
    @WithMockUser(username = "doctor-001", roles = {"DOCTOR"})
    @DisplayName("Création avec données invalides retourne 400")
    void createPrescription_invalidData_returns400() throws Exception {
        PrescriptionCreateRequest invalidRequest = new PrescriptionCreateRequest();
        // patientId manquant - @NotBlank

        mockMvc.perform(post("/api/v1/prescriptions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}

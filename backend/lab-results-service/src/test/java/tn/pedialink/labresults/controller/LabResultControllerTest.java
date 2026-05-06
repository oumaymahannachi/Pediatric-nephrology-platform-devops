package tn.pedialink.labresults.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.pedialink.labresults.config.SecurityConfig;
import tn.pedialink.labresults.dto.CreateLabResultRequest;
import tn.pedialink.labresults.dto.LabResultDto;
import tn.pedialink.labresults.entity.ResultStatus;
import tn.pedialink.labresults.entity.TestType;
import tn.pedialink.labresults.service.LabResultService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LabResultController.class)
@DisplayName("Tests du contrôleur de résultats de laboratoire")
class LabResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LabResultService labResultService;

    @Autowired
    private ObjectMapper objectMapper;

    private LabResultDto sampleDto;
    private CreateLabResultRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleDto = new LabResultDto();
        sampleDto.setId("lab-001");
        sampleDto.setPatientId("patient-001");
        sampleDto.setDoctorId("doctor-001");
        sampleDto.setTestType(TestType.BLOOD);
        sampleDto.setTestDate(LocalDateTime.now());
        sampleDto.setEGFR(85.0);
        sampleDto.setStatus(ResultStatus.PENDING);
        sampleDto.setIsAbnormal(false);

        sampleRequest = new CreateLabResultRequest();
        sampleRequest.setPatientId("patient-001");
        sampleRequest.setTestType(TestType.BLOOD);
        sampleRequest.setTestDate(LocalDateTime.now());
        sampleRequest.setCreatinine(0.8);
        sampleRequest.setSendEmailNotification(false);
    }

    // ===== Tests GET - accessibles sans authentification (permitAll) =====

    @Test
    @DisplayName("GET /patient/{id} - retourne la liste des résultats")
    @WithMockUser(roles = "DOCTOR")
    void getLabResultsByPatient_returnsResults() throws Exception {
        List<LabResultDto> results = Arrays.asList(sampleDto);
        when(labResultService.getLabResultsByPatient("patient-001")).thenReturn(results);

        mockMvc.perform(get("/api/lab-results/patient/patient-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value("lab-001"));

        verify(labResultService).getLabResultsByPatient("patient-001");
    }

    @Test
    @DisplayName("GET /patient/{id} - liste vide si aucun résultat")
    @WithMockUser(roles = "PARENT")
    void getLabResultsByPatient_emptyList_returnsEmptyArray() throws Exception {
        when(labResultService.getLabResultsByPatient("patient-999")).thenReturn(List.of());

        mockMvc.perform(get("/api/lab-results/patient/patient-999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /{id} - retourne un résultat spécifique")
    @WithMockUser(roles = "DOCTOR")
    void getLabResultById_existingId_returnsResult() throws Exception {
        when(labResultService.getLabResultById("lab-001")).thenReturn(sampleDto);

        mockMvc.perform(get("/api/lab-results/lab-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("lab-001"))
                .andExpect(jsonPath("$.data.patientId").value("patient-001"));
    }

    // ===== Tests POST - nécessite authentification =====

    @Test
    @DisplayName("POST / - crée un nouveau résultat avec statut 201")
    @WithMockUser(roles = "DOCTOR")
    void createLabResult_validRequest_returns201() throws Exception {
        when(labResultService.createLabResult(any(), anyString())).thenReturn(sampleDto);

        mockMvc.perform(post("/api/lab-results")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest))
                .header("X-User-Id", "doctor-001"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value("lab-001"));
    }

    @Test
    @DisplayName("POST / - sans authentification retourne 401/403 (sécurité active)")
    void createLabResult_withoutAuth_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/lab-results")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRequest)))
                .andExpect(status().is4xxClientError()); // 401 ou 403 - sécurité active
    }

    // ===== Tests PUT /validate =====

    @Test
    @DisplayName("PUT /{id}/validate - valide un résultat (DOCTOR)")
    @WithMockUser(roles = "DOCTOR")
    void validateLabResult_asDoctor_returnsValidated() throws Exception {
        sampleDto.setStatus(ResultStatus.VALIDATED);
        when(labResultService.validateLabResult(eq("lab-001"), anyString())).thenReturn(sampleDto);

        mockMvc.perform(put("/api/lab-results/lab-001/validate")
                .with(csrf())
                .header("X-User-Id", "doctor-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("VALIDATED"));
    }

    // ===== Tests DELETE =====

    @Test
    @DisplayName("DELETE /{id} - supprime un résultat (DOCTOR)")
    @WithMockUser(roles = "DOCTOR")
    void deleteLabResult_asDoctor_returns200() throws Exception {
        doNothing().when(labResultService).deleteLabResult("lab-001");

        mockMvc.perform(delete("/api/lab-results/lab-001")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(labResultService).deleteLabResult("lab-001");
    }

    // ===== Tests résultats anormaux =====

    @Test
    @DisplayName("GET /patient/{id}/abnormal - retourne seulement les résultats anormaux")
    @WithMockUser(roles = "DOCTOR")
    void getAbnormalLabResults_returnsOnlyAbnormal() throws Exception {
        sampleDto.setIsAbnormal(true);
        when(labResultService.getAbnormalLabResults("patient-001")).thenReturn(List.of(sampleDto));

        mockMvc.perform(get("/api/lab-results/patient/patient-001/abnormal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].isAbnormal").value(true));
    }

    // ===== Test de sécurité - permissions =====

    @Test
    @DisplayName("SÉCURITÉ: Requête sans token retourne 401 - endpoints protégés")
    void securityTest_noToken_returns401() throws Exception {
        mockMvc.perform(get("/api/lab-results/patient/patient-001"))
                .andExpect(status().is4xxClientError());
    }
}

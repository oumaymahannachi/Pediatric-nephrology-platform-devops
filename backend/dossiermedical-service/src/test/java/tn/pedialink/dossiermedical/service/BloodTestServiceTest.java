package tn.pedialink.dossiermedical.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.pedialink.dossiermedical.dto.BloodTestDto;
import tn.pedialink.dossiermedical.model.examen.BloodTest;
import tn.pedialink.dossiermedical.repository.BloodTestRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BloodTestServiceTest {

    @Mock
    private BloodTestRepository bloodTestRepository;

    @InjectMocks
    private BloodTestService bloodTestService;

    private BloodTestDto dto;
    private BloodTest bloodTest;

    @BeforeEach
    void setUp() {
        dto = new BloodTestDto();
        dto.setPatientId("patient-001");
        dto.setPatientName("Sara Trabelsi");
        dto.setMedecinId("doctor-001");
        dto.setMedecinName("Dr. Karim");
        dto.setTestDate(LocalDateTime.now());
        dto.setTestType("CBC");
        dto.setLaboratoryName("Lab Central");
        dto.setAbnormal(false);

        Map<String, BloodTestDto.TestValueDto> results = new HashMap<>();
        BloodTestDto.TestValueDto hb = new BloodTestDto.TestValueDto();
        hb.setValue(12.5);
        hb.setUnit("g/dL");
        hb.setReferenceRange("11.5-15.5");
        hb.setIsAbnormal(false);
        results.put("hemoglobin", hb);
        dto.setResults(results);

        bloodTest = new BloodTest();
        bloodTest.setId("bt-001");
        bloodTest.setPatientId("patient-001");
        bloodTest.setTestType("CBC");
        bloodTest.setAbnormal(false);
    }

    // ===== TESTS CRUD =====

    @Test
    void createBloodTest_shouldSaveAndReturn() {
        when(bloodTestRepository.save(any())).thenReturn(bloodTest);

        BloodTest result = bloodTestService.createBloodTest(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("bt-001");
        verify(bloodTestRepository, times(1)).save(any(BloodTest.class));
    }

    @Test
    void createBloodTest_shouldMapAllFields() {
        when(bloodTestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BloodTest result = bloodTestService.createBloodTest(dto);

        assertThat(result.getPatientId()).isEqualTo("patient-001");
        assertThat(result.getPatientName()).isEqualTo("Sara Trabelsi");
        assertThat(result.getMedecinId()).isEqualTo("doctor-001");
        assertThat(result.getTestType()).isEqualTo("CBC");
        assertThat(result.getLaboratoryName()).isEqualTo("Lab Central");
    }

    @Test
    void createBloodTest_shouldConvertResults() {
        when(bloodTestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BloodTest result = bloodTestService.createBloodTest(dto);

        assertThat(result.getResults()).isNotNull();
        assertThat(result.getResults()).containsKey("hemoglobin");
        assertThat(result.getResults().get("hemoglobin").getValue()).isEqualTo(12.5);
        assertThat(result.getResults().get("hemoglobin").getUnit()).isEqualTo("g/dL");
    }

    @Test
    void createBloodTest_shouldSetCreatedAt() {
        when(bloodTestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BloodTest result = bloodTestService.createBloodTest(dto);

        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void getBloodTestById_shouldReturnTest() {
        when(bloodTestRepository.findById("bt-001")).thenReturn(Optional.of(bloodTest));

        BloodTest result = bloodTestService.getBloodTestById("bt-001");

        assertThat(result.getId()).isEqualTo("bt-001");
    }

    @Test
    void getBloodTestById_notFound_shouldThrowException() {
        when(bloodTestRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bloodTestService.getBloodTestById("invalid"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Blood test not found");
    }

    @Test
    void getBloodTestsByPatient_shouldReturnList() {
        when(bloodTestRepository.findByPatientId("patient-001"))
            .thenReturn(Arrays.asList(bloodTest));

        List<BloodTest> result = bloodTestService.getBloodTestsByPatient("patient-001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPatientId()).isEqualTo("patient-001");
    }

    @Test
    void getBloodTestsByPatient_noResults_shouldReturnEmptyList() {
        when(bloodTestRepository.findByPatientId("unknown"))
            .thenReturn(Collections.emptyList());

        List<BloodTest> result = bloodTestService.getBloodTestsByPatient("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void updateBloodTest_shouldUpdateFields() {
        when(bloodTestRepository.findById("bt-001")).thenReturn(Optional.of(bloodTest));
        when(bloodTestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        dto.setTestType("UPDATED_TYPE");
        BloodTest result = bloodTestService.updateBloodTest("bt-001", dto);

        assertThat(result.getTestType()).isEqualTo("UPDATED_TYPE");
        verify(bloodTestRepository).save(any());
    }

    @Test
    void updateBloodTest_notFound_shouldThrowException() {
        when(bloodTestRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bloodTestService.updateBloodTest("invalid", dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Blood test not found");
    }

    @Test
    void deleteBloodTest_shouldCallRepository() {
        doNothing().when(bloodTestRepository).deleteById("bt-001");

        bloodTestService.deleteBloodTest("bt-001");

        verify(bloodTestRepository, times(1)).deleteById("bt-001");
    }

    // ===== TESTS LOGIQUE MÉTIER : Résultats anormaux =====

    @Test
    void createBloodTest_withAbnormalResults_shouldFlagAbnormal() {
        dto.setAbnormal(true);
        BloodTestDto.TestValueDto abnormalHb = new BloodTestDto.TestValueDto();
        abnormalHb.setValue(7.0);
        abnormalHb.setUnit("g/dL");
        abnormalHb.setIsAbnormal(true);
        dto.getResults().put("hemoglobin", abnormalHb);

        when(bloodTestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BloodTest result = bloodTestService.createBloodTest(dto);

        assertThat(result.getAbnormal()).isTrue();
        assertThat(result.getResults().get("hemoglobin").getIsAbnormal()).isTrue();
    }
}

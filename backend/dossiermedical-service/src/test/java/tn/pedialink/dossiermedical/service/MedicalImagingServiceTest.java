package tn.pedialink.dossiermedical.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.pedialink.dossiermedical.dto.MedicalImagingDto;
import tn.pedialink.dossiermedical.model.examen.MedicalImaging;
import tn.pedialink.dossiermedical.repository.MedicalImagingRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalImagingServiceTest {

    @Mock
    private MedicalImagingRepository medicalImagingRepository;

    @InjectMocks
    private MedicalImagingService medicalImagingService;

    private MedicalImagingDto dto;
    private MedicalImaging imaging;

    @BeforeEach
    void setUp() {
        dto = new MedicalImagingDto();
        dto.setPatientId("patient-001");
        dto.setPatientName("Leila Mansour");
        dto.setMedecinId("doctor-001");
        dto.setImagingDate(LocalDateTime.now());
        dto.setImagingType("ULTRASOUND");
        dto.setBodyPart("Kidney");
        dto.setFindings("Normal kidney size");
        dto.setImpression("No abnormality detected");
        dto.setUrgencyLevel("NORMAL");
        dto.setFollowUpRequired(false);
        dto.setStatus("COMPLETED");
        dto.setAbnormal(false);

        imaging = new MedicalImaging();
        imaging.setId("img-001");
        imaging.setPatientId("patient-001");
        imaging.setImagingType("ULTRASOUND");
        imaging.setStatus("COMPLETED");
    }

    @Test
    void createMedicalImaging_shouldSaveAndReturn() {
        when(medicalImagingRepository.save(any())).thenReturn(imaging);

        MedicalImaging result = medicalImagingService.createMedicalImaging(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("img-001");
        verify(medicalImagingRepository, times(1)).save(any());
    }

    @Test
    void createMedicalImaging_shouldMapAllFields() {
        when(medicalImagingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MedicalImaging result = medicalImagingService.createMedicalImaging(dto);

        assertThat(result.getPatientId()).isEqualTo("patient-001");
        assertThat(result.getImagingType()).isEqualTo("ULTRASOUND");
        assertThat(result.getBodyPart()).isEqualTo("Kidney");
        assertThat(result.getFindings()).isEqualTo("Normal kidney size");
        assertThat(result.getUrgencyLevel()).isEqualTo("NORMAL");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void createMedicalImaging_shouldSetTimestamps() {
        when(medicalImagingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MedicalImaging result = medicalImagingService.createMedicalImaging(dto);

        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    void createMedicalImaging_urgentCase_shouldFlagCorrectly() {
        dto.setUrgencyLevel("HIGH");
        dto.setAbnormal(true);
        dto.setFollowUpRequired(true);

        when(medicalImagingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MedicalImaging result = medicalImagingService.createMedicalImaging(dto);

        assertThat(result.getUrgencyLevel()).isEqualTo("HIGH");
        assertThat(result.getAbnormal()).isTrue();
        assertThat(result.getFollowUpRequired()).isTrue();
    }

    @Test
    void getMedicalImagingById_shouldReturnImaging() {
        when(medicalImagingRepository.findById("img-001")).thenReturn(Optional.of(imaging));

        MedicalImaging result = medicalImagingService.getMedicalImagingById("img-001");

        assertThat(result.getId()).isEqualTo("img-001");
    }

    @Test
    void getMedicalImagingById_notFound_shouldThrowException() {
        when(medicalImagingRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalImagingService.getMedicalImagingById("invalid"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Medical imaging not found");
    }

    @Test
    void getMedicalImagingByPatient_shouldReturnList() {
        when(medicalImagingRepository.findByPatientId("patient-001"))
            .thenReturn(Arrays.asList(imaging));

        List<MedicalImaging> result = medicalImagingService.getMedicalImagingByPatient("patient-001");

        assertThat(result).hasSize(1);
    }

    @Test
    void getMedicalImagingByPatient_noResults_shouldReturnEmpty() {
        when(medicalImagingRepository.findByPatientId("unknown"))
            .thenReturn(Collections.emptyList());

        List<MedicalImaging> result = medicalImagingService.getMedicalImagingByPatient("unknown");

        assertThat(result).isEmpty();
    }

    @Test
    void updateMedicalImaging_shouldUpdateFields() {
        when(medicalImagingRepository.findById("img-001")).thenReturn(Optional.of(imaging));
        when(medicalImagingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        dto.setFindings("Updated findings");
        dto.setStatus("REVIEWED");

        MedicalImaging result = medicalImagingService.updateMedicalImaging("img-001", dto);

        assertThat(result.getFindings()).isEqualTo("Updated findings");
        assertThat(result.getStatus()).isEqualTo("REVIEWED");
    }

    @Test
    void updateMedicalImaging_notFound_shouldThrowException() {
        when(medicalImagingRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalImagingService.updateMedicalImaging("invalid", dto))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Medical imaging not found");
    }

    @Test
    void deleteMedicalImaging_shouldCallRepository() {
        doNothing().when(medicalImagingRepository).deleteById("img-001");

        medicalImagingService.deleteMedicalImaging("img-001");

        verify(medicalImagingRepository, times(1)).deleteById("img-001");
    }
}

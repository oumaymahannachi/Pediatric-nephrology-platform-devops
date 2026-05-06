package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.LabResultDto;
import tn.pedialink.dossiermedical.model.examen.LabResult;
import tn.pedialink.dossiermedical.repository.LabResultRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabResultService {
    private final LabResultRepository labResultRepository;

    public LabResult createLabResult(LabResultDto dto) {
        LabResult labResult = new LabResult();
        labResult.setPatientId(dto.getPatientId());
        labResult.setPatientName(dto.getPatientName());
        labResult.setMedecinId(dto.getMedecinId());
        labResult.setMedecinName(dto.getMedecinName());
        labResult.setTestDate(dto.getTestDate());
        labResult.setTestType(dto.getTestType());
        labResult.setTestName(dto.getTestName());
        labResult.setFindings(dto.getFindings());
        labResult.setResult(dto.getResult());
        labResult.setDetails(dto.getDetails());
        labResult.setLaboratoryName(dto.getLaboratoryName());
        labResult.setSpecimenType(dto.getSpecimenType());
        labResult.setNotes(dto.getNotes());
        labResult.setAbnormal(dto.getAbnormal());
        labResult.setCreatedAt(LocalDateTime.now());
        labResult.setUpdatedAt(LocalDateTime.now());
        return labResultRepository.save(labResult);
    }

    public LabResult updateLabResult(String id, LabResultDto dto) {
        LabResult labResult = labResultRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lab result not found"));
        labResult.setTestDate(dto.getTestDate());
        labResult.setTestType(dto.getTestType());
        labResult.setTestName(dto.getTestName());
        labResult.setFindings(dto.getFindings());
        labResult.setResult(dto.getResult());
        labResult.setDetails(dto.getDetails());
        labResult.setLaboratoryName(dto.getLaboratoryName());
        labResult.setSpecimenType(dto.getSpecimenType());
        labResult.setNotes(dto.getNotes());
        labResult.setAbnormal(dto.getAbnormal());
        labResult.setUpdatedAt(LocalDateTime.now());
        return labResultRepository.save(labResult);
    }

    public LabResult getLabResultById(String id) {
        return labResultRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lab result not found"));
    }

    public List<LabResult> getLabResultsByPatient(String patientId) {
        return labResultRepository.findByPatientId(patientId);
    }

    public void deleteLabResult(String id) {
        labResultRepository.deleteById(id);
    }
}

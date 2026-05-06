package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.BloodTestDto;
import tn.pedialink.dossiermedical.model.examen.BloodTest;
import tn.pedialink.dossiermedical.repository.BloodTestRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BloodTestService {
    private final BloodTestRepository bloodTestRepository;

    public BloodTest createBloodTest(BloodTestDto dto) {
        BloodTest bloodTest = new BloodTest();
        bloodTest.setPatientId(dto.getPatientId());
        bloodTest.setPatientName(dto.getPatientName());
        bloodTest.setMedecinId(dto.getMedecinId());
        bloodTest.setMedecinName(dto.getMedecinName());
        bloodTest.setTestDate(dto.getTestDate());
        bloodTest.setTestType(dto.getTestType());
        bloodTest.setResults(convertResults(dto.getResults()));
        bloodTest.setLaboratoryName(dto.getLaboratoryName());
        bloodTest.setNotes(dto.getNotes());
        bloodTest.setInterpretation(dto.getInterpretation());
        bloodTest.setAbnormal(dto.getAbnormal());
        bloodTest.setCreatedAt(LocalDateTime.now());
        bloodTest.setUpdatedAt(LocalDateTime.now());
        return bloodTestRepository.save(bloodTest);
    }

    public BloodTest updateBloodTest(String id, BloodTestDto dto) {
        BloodTest bloodTest = bloodTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blood test not found"));
        bloodTest.setTestDate(dto.getTestDate());
        bloodTest.setTestType(dto.getTestType());
        bloodTest.setResults(convertResults(dto.getResults()));
        bloodTest.setLaboratoryName(dto.getLaboratoryName());
        bloodTest.setNotes(dto.getNotes());
        bloodTest.setInterpretation(dto.getInterpretation());
        bloodTest.setAbnormal(dto.getAbnormal());
        bloodTest.setUpdatedAt(LocalDateTime.now());
        return bloodTestRepository.save(bloodTest);
    }

    public BloodTest getBloodTestById(String id) {
        return bloodTestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Blood test not found"));
    }

    public List<BloodTest> getBloodTestsByPatient(String patientId) {
        return bloodTestRepository.findByPatientId(patientId);
    }

    public void deleteBloodTest(String id) {
        bloodTestRepository.deleteById(id);
    }

    private Map<String, BloodTest.TestValue> convertResults(Map<String, BloodTestDto.TestValueDto> dtoResults) {
        if (dtoResults == null) return null;
        Map<String, BloodTest.TestValue> results = new HashMap<>();
        dtoResults.forEach((key, dtoValue) -> {
            BloodTest.TestValue value = new BloodTest.TestValue();
            value.setValue(dtoValue.getValue());
            value.setUnit(dtoValue.getUnit());
            value.setReferenceRange(dtoValue.getReferenceRange());
            value.setIsAbnormal(dtoValue.getIsAbnormal());
            results.put(key, value);
        });
        return results;
    }
}

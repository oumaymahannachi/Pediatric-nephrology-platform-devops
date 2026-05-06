package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.DialysisPrescriptionDto;
import tn.pedialink.dossiermedical.model.dialyse.DialysisPrescription;
import tn.pedialink.dossiermedical.repository.DialysisPrescriptionRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DialysisPrescriptionService {
    private final DialysisPrescriptionRepository prescriptionRepository;

    public DialysisPrescription createPrescription(DialysisPrescriptionDto dto) {
        DialysisPrescription prescription = new DialysisPrescription();
        prescription.setPatientId(dto.getPatientId());
        prescription.setPatientName(dto.getPatientName());
        prescription.setMedecinId(dto.getMedecinId());
        prescription.setType(dto.getType());
        prescription.setFrequencyPerWeek(dto.getFrequencyPerWeek());
        prescription.setSessionDurationMinutes(dto.getSessionDurationMinutes());
        prescription.setBloodFlowRate(dto.getBloodFlowRate());
        prescription.setDialysateFlowRate(dto.getDialysateFlowRate());
        prescription.setAnticoagulation(dto.getAnticoagulation());
        prescription.setVascularAccess(dto.getVascularAccess());
        prescription.setNotes(dto.getNotes());
        prescription.setStartDate(dto.getStartDate());
        prescription.setEndDate(dto.getEndDate());
        prescription.setActive(true);
        prescription.setCreatedAt(LocalDateTime.now());
        prescription.setUpdatedAt(LocalDateTime.now());
        return prescriptionRepository.save(prescription);
    }

    public DialysisPrescription updatePrescription(String id, DialysisPrescriptionDto dto) {
        DialysisPrescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prescription not found"));
        
        prescription.setType(dto.getType());
        prescription.setFrequencyPerWeek(dto.getFrequencyPerWeek());
        prescription.setSessionDurationMinutes(dto.getSessionDurationMinutes());
        prescription.setBloodFlowRate(dto.getBloodFlowRate());
        prescription.setDialysateFlowRate(dto.getDialysateFlowRate());
        prescription.setAnticoagulation(dto.getAnticoagulation());
        prescription.setVascularAccess(dto.getVascularAccess());
        prescription.setNotes(dto.getNotes());
        prescription.setStartDate(dto.getStartDate());
        prescription.setEndDate(dto.getEndDate());
        prescription.setUpdatedAt(LocalDateTime.now());
        return prescriptionRepository.save(prescription);
    }

    public void deactivatePrescription(String id) {
        DialysisPrescription prescription = prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prescription not found"));
        prescription.setActive(false);
        prescription.setUpdatedAt(LocalDateTime.now());
        prescriptionRepository.save(prescription);
    }

    public List<DialysisPrescription> getPrescriptionsByPatient(String patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public List<DialysisPrescription> getActivePrescriptionsByPatient(String patientId) {
        return prescriptionRepository.findByPatientIdAndActive(patientId, true);
    }

    public List<DialysisPrescription> getPrescriptionsByDoctor(String medecinId) {
        return prescriptionRepository.findByMedecinId(medecinId);
    }

    public DialysisPrescription getPrescriptionById(String id) {
        return prescriptionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Prescription not found"));
    }
}

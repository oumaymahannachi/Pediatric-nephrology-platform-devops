package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.MedicalImagingDto;
import tn.pedialink.dossiermedical.model.examen.MedicalImaging;
import tn.pedialink.dossiermedical.repository.MedicalImagingRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalImagingService {
    private final MedicalImagingRepository medicalImagingRepository;

    public MedicalImaging createMedicalImaging(MedicalImagingDto dto) {
        MedicalImaging imaging = new MedicalImaging();
        imaging.setPatientId(dto.getPatientId());
        imaging.setPatientName(dto.getPatientName());
        imaging.setMedecinId(dto.getMedecinId());
        imaging.setMedecinName(dto.getMedecinName());
        imaging.setImagingDate(dto.getImagingDate());
        imaging.setImagingType(dto.getImagingType());
        imaging.setBodyPart(dto.getBodyPart());
        imaging.setIndication(dto.getIndication());
        imaging.setFindings(dto.getFindings());
        imaging.setImpression(dto.getImpression());
        imaging.setRecommendation(dto.getRecommendation());
        imaging.setRadiologistName(dto.getRadiologistName());
        imaging.setPerformedBy(dto.getPerformedBy());
        imaging.setFacilityName(dto.getFacilityName());
        imaging.setImageUrls(dto.getImageUrls());
        imaging.setDocumentUrls(dto.getDocumentUrls());
        imaging.setUrgencyLevel(dto.getUrgencyLevel());
        imaging.setFollowUpRequired(dto.getFollowUpRequired());
        imaging.setFollowUpDate(dto.getFollowUpDate());
        imaging.setStatus(dto.getStatus());
        imaging.setNotes(dto.getNotes());
        imaging.setAbnormal(dto.getAbnormal());
        imaging.setCreatedAt(LocalDateTime.now());
        imaging.setUpdatedAt(LocalDateTime.now());
        return medicalImagingRepository.save(imaging);
    }

    public MedicalImaging updateMedicalImaging(String id, MedicalImagingDto dto) {
        MedicalImaging imaging = medicalImagingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Medical imaging not found"));
        imaging.setImagingDate(dto.getImagingDate());
        imaging.setImagingType(dto.getImagingType());
        imaging.setBodyPart(dto.getBodyPart());
        imaging.setIndication(dto.getIndication());
        imaging.setFindings(dto.getFindings());
        imaging.setImpression(dto.getImpression());
        imaging.setRecommendation(dto.getRecommendation());
        imaging.setRadiologistName(dto.getRadiologistName());
        imaging.setPerformedBy(dto.getPerformedBy());
        imaging.setFacilityName(dto.getFacilityName());
        imaging.setImageUrls(dto.getImageUrls());
        imaging.setDocumentUrls(dto.getDocumentUrls());
        imaging.setUrgencyLevel(dto.getUrgencyLevel());
        imaging.setFollowUpRequired(dto.getFollowUpRequired());
        imaging.setFollowUpDate(dto.getFollowUpDate());
        imaging.setStatus(dto.getStatus());
        imaging.setNotes(dto.getNotes());
        imaging.setAbnormal(dto.getAbnormal());
        imaging.setUpdatedAt(LocalDateTime.now());
        return medicalImagingRepository.save(imaging);
    }

    public MedicalImaging getMedicalImagingById(String id) {
        return medicalImagingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Medical imaging not found"));
    }

    public List<MedicalImaging> getMedicalImagingByPatient(String patientId) {
        return medicalImagingRepository.findByPatientId(patientId);
    }

    public void deleteMedicalImaging(String id) {
        medicalImagingRepository.deleteById(id);
    }
}

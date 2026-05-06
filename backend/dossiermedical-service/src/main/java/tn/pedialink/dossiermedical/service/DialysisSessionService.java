package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.DialysisSessionDto;
import tn.pedialink.dossiermedical.model.dialyse.DialysisSession;
import tn.pedialink.dossiermedical.model.dialyse.StatutSession;
import tn.pedialink.dossiermedical.repository.DialysisSessionRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DialysisSessionService {
    private final DialysisSessionRepository sessionRepository;

    public DialysisSession scheduleSession(DialysisSessionDto dto) {
        DialysisSession session = new DialysisSession();
        session.setPrescriptionId(dto.getPrescriptionId());
        session.setPatientId(dto.getPatientId());
        session.setPatientName(dto.getPatientName());
        session.setMedecinId(dto.getMedecinId());
        session.setScheduledDate(dto.getScheduledDate());
        session.setStatus(StatutSession.SCHEDULED);
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public DialysisSession startSession(String id) {
        DialysisSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStartTime(LocalDateTime.now());
        session.setStatus(StatutSession.IN_PROGRESS);
        session.setUpdatedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public DialysisSession completeSession(String id, DialysisSessionDto dto) {
        DialysisSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setEndTime(LocalDateTime.now());
        session.setStatus(StatutSession.COMPLETED);
        
        // Update vitals and session data
        session.setPreWeight(dto.getPreWeight());
        session.setPreBloodPressureSystolic(dto.getPreBloodPressureSystolic());
        session.setPreBloodPressureDiastolic(dto.getPreBloodPressureDiastolic());
        session.setPrePulse(dto.getPrePulse());
        session.setPreTemperature(dto.getPreTemperature());
        
        session.setPostWeight(dto.getPostWeight());
        session.setPostBloodPressureSystolic(dto.getPostBloodPressureSystolic());
        session.setPostBloodPressureDiastolic(dto.getPostBloodPressureDiastolic());
        session.setPostPulse(dto.getPostPulse());
        session.setPostTemperature(dto.getPostTemperature());
        
        session.setUltrafiltrationVolume(dto.getUltrafiltrationVolume());
        session.setComplications(dto.getComplications());
        session.setNotes(dto.getNotes());
        session.setUpdatedAt(LocalDateTime.now());
        
        return sessionRepository.save(session);
    }

    public DialysisSession cancelSession(String id, String reason) {
        DialysisSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus(StatutSession.CANCELLED);
        session.setNotes(reason);
        session.setUpdatedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public DialysisSession rescheduleSession(String id, LocalDateTime newDate) {
        DialysisSession session = sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        session.setScheduledDate(newDate);
        session.setStatus(StatutSession.SCHEDULED);
        session.setUpdatedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    public List<DialysisSession> getSessionsByPatient(String patientId) {
        return sessionRepository.findByPatientId(patientId);
    }

    public List<DialysisSession> getSessionsByPrescription(String prescriptionId) {
        return sessionRepository.findByPrescriptionId(prescriptionId);
    }

    public List<DialysisSession> getSessionsByDoctor(String medecinId) {
        return sessionRepository.findByMedecinId(medecinId);
    }

    public List<DialysisSession> getSessionsByDateRange(LocalDateTime start, LocalDateTime end) {
        return sessionRepository.findByScheduledDateBetween(start, end);
    }

    public DialysisSession getSessionById(String id) {
        return sessionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Session not found"));
    }
}

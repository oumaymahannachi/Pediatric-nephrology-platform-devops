package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.ExamenDto;
import tn.pedialink.dossiermedical.model.examen.Examen;
import tn.pedialink.dossiermedical.model.examen.TypeExamen;
import tn.pedialink.dossiermedical.repository.ExamenRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamenService {
    private final ExamenRepository examenRepository;

    public Examen createExamen(ExamenDto dto) {
        Examen examen = new Examen();
        examen.setPatientId(dto.getPatientId());
        examen.setMedecinId(dto.getMedecinId());
        examen.setConsultationId(dto.getConsultationId());
        examen.setTypeExamen(dto.getTypeExamen());
        examen.setNomExamen(dto.getNomExamen());
        examen.setDateExamen(dto.getDateExamen());
        examen.setResultats(dto.getResultats());
        examen.setInterpretation(dto.getInterpretation());
        examen.setLaboratoire(dto.getLaboratoire());
        examen.setFichierUrl(dto.getFichierUrl());
        examen.setCreatedAt(LocalDateTime.now());
        examen.setUpdatedAt(LocalDateTime.now());
        return examenRepository.save(examen);
    }

    public Examen updateExamen(String id, ExamenDto dto) {
        Examen examen = examenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Examen non trouvé"));
        examen.setTypeExamen(dto.getTypeExamen());
        examen.setNomExamen(dto.getNomExamen());
        examen.setDateExamen(dto.getDateExamen());
        examen.setResultats(dto.getResultats());
        examen.setInterpretation(dto.getInterpretation());
        examen.setLaboratoire(dto.getLaboratoire());
        examen.setFichierUrl(dto.getFichierUrl());
        examen.setUpdatedAt(LocalDateTime.now());
        return examenRepository.save(examen);
    }

    public Examen getExamenById(String id) {
        return examenRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Examen non trouvé"));
    }

    public List<Examen> getExamensByPatient(String patientId) {
        return examenRepository.findByPatientId(patientId);
    }

    public List<Examen> getExamensByPatientAndType(String patientId, TypeExamen typeExamen) {
        return examenRepository.findByPatientIdAndTypeExamen(patientId, typeExamen);
    }

    public List<Examen> getAllExamens() {
        return examenRepository.findAll();
    }

    public void deleteExamen(String id) {
        examenRepository.deleteById(id);
    }
}

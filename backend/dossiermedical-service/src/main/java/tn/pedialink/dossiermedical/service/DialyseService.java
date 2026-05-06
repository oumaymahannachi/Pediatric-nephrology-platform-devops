package tn.pedialink.dossiermedical.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.dossiermedical.dto.DialyseDto;
import tn.pedialink.dossiermedical.model.dialyse.Dialyse;
import tn.pedialink.dossiermedical.repository.DialyseRepository;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DialyseService {
    private final DialyseRepository dialyseRepository;

    public Dialyse createDialyse(DialyseDto dto) {
        Dialyse dialyse = new Dialyse();
        dialyse.setPatientId(dto.getPatientId());
        dialyse.setMedecinId(dto.getMedecinId());
        dialyse.setConsultationId(dto.getConsultationId());
        dialyse.setDateSeance(dto.getDateSeance());
        dialyse.setTypeDialyse(dto.getTypeDialyse());
        dialyse.setParametres(dto.getParametres());
        dialyse.setAccesVasculaire(dto.getAccesVasculaire());
        dialyse.setComplications(dto.getComplications());
        dialyse.setObservations(dto.getObservations());
        dialyse.setStatut(dto.getStatut());
        dialyse.setCreatedAt(LocalDateTime.now());
        dialyse.setUpdatedAt(LocalDateTime.now());
        return dialyseRepository.save(dialyse);
    }

    public Dialyse updateDialyse(String id, DialyseDto dto) {
        Dialyse dialyse = dialyseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dialyse non trouvée"));
        dialyse.setDateSeance(dto.getDateSeance());
        dialyse.setTypeDialyse(dto.getTypeDialyse());
        dialyse.setParametres(dto.getParametres());
        dialyse.setAccesVasculaire(dto.getAccesVasculaire());
        dialyse.setComplications(dto.getComplications());
        dialyse.setObservations(dto.getObservations());
        dialyse.setStatut(dto.getStatut());
        dialyse.setUpdatedAt(LocalDateTime.now());
        return dialyseRepository.save(dialyse);
    }

    public Dialyse getDialyseById(String id) {
        return dialyseRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dialyse non trouvée"));
    }

    public List<Dialyse> getDialysesByPatient(String patientId) {
        return dialyseRepository.findByPatientId(patientId);
    }

    public List<Dialyse> getAllDialyses() {
        return dialyseRepository.findAll();
    }

    public void deleteDialyse(String id) {
        dialyseRepository.deleteById(id);
    }
}

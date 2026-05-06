package tn.pedialink.treatment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.treatment.entity.DietaryRestriction;
import tn.pedialink.treatment.entity.GrowthMeasurement;
import tn.pedialink.treatment.exception.NotFoundException;
import tn.pedialink.treatment.h2.service.TreatmentH2BackupService;
import tn.pedialink.treatment.repository.DietaryRestrictionRepository;
import tn.pedialink.treatment.repository.GrowthMeasurementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrowthService {

    private final GrowthMeasurementRepository growthRepo;
    private final DietaryRestrictionRepository dietaryRepo;
    private final TreatmentH2BackupService h2BackupService;

    public List<GrowthMeasurement> getMeasurements(String childId) {
        return growthRepo.findByChildIdOrderByDateDesc(childId);
    }

    public GrowthMeasurement addMeasurement(GrowthMeasurement measurement) {
        measurement.calculateBmi();
        GrowthMeasurement saved = growthRepo.save(measurement);
        h2BackupService.backupGrowthMeasurement(saved);
        return saved;
    }

    public GrowthMeasurement updateMeasurement(String id, GrowthMeasurement data) {
        GrowthMeasurement existing = growthRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Measurement not found"));
        existing.setDate(data.getDate());
        existing.setWeight(data.getWeight());
        existing.setHeight(data.getHeight());
        existing.setHeadCircumference(data.getHeadCircumference());
        existing.setNotes(data.getNotes());
        existing.calculateBmi();
        GrowthMeasurement saved = growthRepo.save(existing);
        h2BackupService.backupGrowthMeasurement(saved);
        return saved;
    }

    public void deleteMeasurement(String id) {
        if (!growthRepo.existsById(id)) {
            throw new NotFoundException("Measurement not found");
        }
        growthRepo.deleteById(id);
        h2BackupService.deleteGrowthMeasurement(id);
    }

    public List<DietaryRestriction> getRestrictions(String childId) {
        return dietaryRepo.findByChildId(childId);
    }

    public DietaryRestriction addRestriction(DietaryRestriction restriction) {
        DietaryRestriction saved = dietaryRepo.save(restriction);
        h2BackupService.backupDietaryRestriction(saved);
        return saved;
    }

    public DietaryRestriction updateRestriction(String id, DietaryRestriction data) {
        DietaryRestriction existing = dietaryRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Restriction not found"));
        existing.setType(data.getType());
        existing.setAllergen(data.getAllergen());
        existing.setSeverity(data.getSeverity());
        existing.setDescription(data.getDescription());
        existing.setNotes(data.getNotes());
        DietaryRestriction saved = dietaryRepo.save(existing);
        h2BackupService.backupDietaryRestriction(saved);
        return saved;
    }

    public void deleteRestriction(String id) {
        if (!dietaryRepo.existsById(id)) {
            throw new NotFoundException("Restriction not found");
        }
        dietaryRepo.deleteById(id);
        h2BackupService.deleteDietaryRestriction(id);
    }

    public long countMeasurements(String childId) {
        return growthRepo.countByChildId(childId);
    }

    public long countAllMeasurements() {
        return growthRepo.count();
    }

    public List<GrowthMeasurement> getAllMeasurements() {
        return growthRepo.findAll();
    }

    public List<DietaryRestriction> getAllRestrictions() {
        return dietaryRepo.findAll();
    }
}

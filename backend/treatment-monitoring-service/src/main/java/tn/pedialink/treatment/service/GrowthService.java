package tn.pedialink.treatment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.treatment.entity.DietaryRestriction;
import tn.pedialink.treatment.entity.GrowthMeasurement;
import tn.pedialink.treatment.exception.NotFoundException;
import tn.pedialink.treatment.repository.DietaryRestrictionRepository;
import tn.pedialink.treatment.repository.GrowthMeasurementRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrowthService {

    private final GrowthMeasurementRepository growthRepo;
    private final DietaryRestrictionRepository dietaryRepo;

    public List<GrowthMeasurement> getMeasurements(String childId) {
        return growthRepo.findByChildIdOrderByDateDesc(childId);
    }

    public GrowthMeasurement addMeasurement(GrowthMeasurement measurement) {
        measurement.calculateBmi();
        return growthRepo.save(measurement);
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
        return growthRepo.save(existing);
    }

    public void deleteMeasurement(String id) {
        if (!growthRepo.existsById(id)) {
            throw new NotFoundException("Measurement not found");
        }
        growthRepo.deleteById(id);
    }

    public List<DietaryRestriction> getRestrictions(String childId) {
        return dietaryRepo.findByChildId(childId);
    }

    public DietaryRestriction addRestriction(DietaryRestriction restriction) {
        return dietaryRepo.save(restriction);
    }

    public DietaryRestriction updateRestriction(String id, DietaryRestriction data) {
        DietaryRestriction existing = dietaryRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Restriction not found"));
        existing.setType(data.getType());
        existing.setAllergen(data.getAllergen());
        existing.setSeverity(data.getSeverity());
        existing.setDescription(data.getDescription());
        existing.setNotes(data.getNotes());
        return dietaryRepo.save(existing);
    }

    public void deleteRestriction(String id) {
        if (!dietaryRepo.existsById(id)) {
            throw new NotFoundException("Restriction not found");
        }
        dietaryRepo.deleteById(id);
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

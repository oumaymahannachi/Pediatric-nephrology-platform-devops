package tn.pedialink.treatment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.treatment.entity.NutritionalPlan;
import tn.pedialink.treatment.exception.BadRequestException;
import tn.pedialink.treatment.exception.NotFoundException;
import tn.pedialink.treatment.repository.NutritionalPlanRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NutritionalPlanService {

    private final NutritionalPlanRepository planRepo;

    public List<NutritionalPlan> getPlansByChild(String childId) {
        return planRepo.findByChildId(childId);
    }

    public List<NutritionalPlan> getPlansByDoctor(String doctorId) {
        return planRepo.findByDoctorId(doctorId);
    }

    public NutritionalPlan createPlan(String doctorId, NutritionalPlan plan) {
        plan.setDoctorId(doctorId);
        return planRepo.save(plan);
    }

    public NutritionalPlan updatePlan(String doctorId, String planId, NutritionalPlan data) {
        NutritionalPlan existing = planRepo.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        if (!existing.getDoctorId().equals(doctorId)) {
            throw new BadRequestException("Not authorized to update this plan");
        }
        existing.setTitle(data.getTitle());
        existing.setDescription(data.getDescription());
        existing.setStartDate(data.getStartDate());
        existing.setEndDate(data.getEndDate());
        existing.setStatus(data.getStatus());
        existing.setGoals(data.getGoals());
        existing.setRestrictions(data.getRestrictions());
        existing.setMeals(data.getMeals());
        return planRepo.save(existing);
    }

    public void deletePlan(String doctorId, String planId) {
        NutritionalPlan existing = planRepo.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
        if (!existing.getDoctorId().equals(doctorId)) {
            throw new BadRequestException("Not authorized to delete this plan");
        }
        planRepo.deleteById(planId);
    }

    public NutritionalPlan getPlanById(String planId) {
        return planRepo.findById(planId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
    }

    public long countByDoctor(String doctorId) {
        return planRepo.countByDoctorId(doctorId);
    }

    public List<NutritionalPlan> getAllPlans() {
        return planRepo.findAll();
    }

    public long countAll() {
        return planRepo.count();
    }
}

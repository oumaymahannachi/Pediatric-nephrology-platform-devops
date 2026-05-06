package tn.pedialink.treatment.h2.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.pedialink.treatment.entity.*;
import tn.pedialink.treatment.h2.entity.*;
import tn.pedialink.treatment.h2.repository.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TreatmentH2BackupService {

    private final ChildH2Repository childH2Repo;
    private final GrowthMeasurementH2Repository growthH2Repo;
    private final AppointmentH2Repository appointmentH2Repo;
    private final NutritionalPlanH2Repository planH2Repo;
    private final DietaryRestrictionH2Repository dietaryH2Repo;

    @Async
    @Transactional("h2TransactionManager")
    public void backupChild(Child child) {
        try {
            ChildH2 h2 = childH2Repo.findByMongoId(child.getId())
                    .orElse(ChildH2.builder().mongoId(child.getId()).build());
            h2.setFullName(child.getFullName());
            h2.setDateOfBirth(child.getDateOfBirth());
            h2.setGender(child.getGender());
            h2.setParentId(child.getParentId());
            h2.setDoctorIds(child.getDoctorIds());
            h2.setNotes(child.getNotes());
            h2.setCreatedAt(child.getCreatedAt());
            h2.setUpdatedAt(child.getUpdatedAt());
            childH2Repo.save(h2);
            log.debug("[H2-Backup] Child saved: {}", child.getId());
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to backup Child {}: {}", child.getId(), ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void deleteChild(String mongoId) {
        try {
            childH2Repo.findByMongoId(mongoId).ifPresent(c -> {
                childH2Repo.delete(c);
                log.debug("[H2-Backup] Child deleted: {}", mongoId);
            });
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to delete Child {}: {}", mongoId, ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void backupGrowthMeasurement(GrowthMeasurement gm) {
        try {
            GrowthMeasurementH2 h2 = growthH2Repo.findByMongoId(gm.getId())
                    .orElse(GrowthMeasurementH2.builder().mongoId(gm.getId()).build());
            h2.setChildId(gm.getChildId());
            h2.setDate(gm.getDate());
            h2.setWeight(gm.getWeight());
            h2.setHeight(gm.getHeight());
            h2.setBmi(gm.getBmi());
            h2.setHeadCircumference(gm.getHeadCircumference());
            h2.setNotes(gm.getNotes());
            h2.setRecordedBy(gm.getRecordedBy());
            h2.setCreatedAt(gm.getCreatedAt());
            growthH2Repo.save(h2);
            log.debug("[H2-Backup] GrowthMeasurement saved: {}", gm.getId());
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to backup GrowthMeasurement {}: {}", gm.getId(), ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void deleteGrowthMeasurement(String mongoId) {
        try {
            growthH2Repo.findByMongoId(mongoId).ifPresent(h -> {
                growthH2Repo.delete(h);
                log.debug("[H2-Backup] GrowthMeasurement deleted: {}", mongoId);
            });
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to delete GrowthMeasurement {}: {}", mongoId, ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void backupAppointment(Appointment apt) {
        try {
            AppointmentH2 h2 = appointmentH2Repo.findByMongoId(apt.getId())
                    .orElse(AppointmentH2.builder().mongoId(apt.getId()).build());
            h2.setChildId(apt.getChildId());
            h2.setDoctorId(apt.getDoctorId());
            h2.setParentId(apt.getParentId());
            h2.setDateTime(apt.getDateTime());
            h2.setProposedDateTime(apt.getProposedDateTime());
            h2.setStatus(apt.getStatus() != null ? apt.getStatus().name() : "PENDING");
            h2.setReason(apt.getReason());
            h2.setParentNotes(apt.getParentNotes());
            h2.setDoctorNotes(apt.getDoctorNotes());
            h2.setCreatedAt(apt.getCreatedAt());
            h2.setUpdatedAt(apt.getUpdatedAt());
            appointmentH2Repo.save(h2);
            log.debug("[H2-Backup] Appointment saved: {}", apt.getId());
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to backup Appointment {}: {}", apt.getId(), ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void deleteAppointment(String mongoId) {
        try {
            appointmentH2Repo.findByMongoId(mongoId).ifPresent(a -> {
                appointmentH2Repo.delete(a);
                log.debug("[H2-Backup] Appointment deleted: {}", mongoId);
            });
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to delete Appointment {}: {}", mongoId, ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void backupNutritionalPlan(NutritionalPlan plan) {
        try {
            NutritionalPlanH2 h2 = planH2Repo.findByMongoId(plan.getId())
                    .orElse(NutritionalPlanH2.builder().mongoId(plan.getId()).build());
            h2.setChildId(plan.getChildId());
            h2.setDoctorId(plan.getDoctorId());
            h2.setTitle(plan.getTitle());
            h2.setDescription(plan.getDescription());
            h2.setStartDate(plan.getStartDate());
            h2.setEndDate(plan.getEndDate());
            h2.setStatus(plan.getStatus());
            h2.setGoals(plan.getGoals());
            h2.setRestrictions(plan.getRestrictions());
            h2.setMeals(plan.getMeals());
            h2.setCreatedAt(plan.getCreatedAt());
            h2.setUpdatedAt(plan.getUpdatedAt());
            planH2Repo.save(h2);
            log.debug("[H2-Backup] NutritionalPlan saved: {}", plan.getId());
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to backup NutritionalPlan {}: {}", plan.getId(), ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void deleteNutritionalPlan(String mongoId) {
        try {
            planH2Repo.findByMongoId(mongoId).ifPresent(p -> {
                planH2Repo.delete(p);
                log.debug("[H2-Backup] NutritionalPlan deleted: {}", mongoId);
            });
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to delete NutritionalPlan {}: {}", mongoId, ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void backupDietaryRestriction(DietaryRestriction dr) {
        try {
            DietaryRestrictionH2 h2 = dietaryH2Repo.findByMongoId(dr.getId())
                    .orElse(DietaryRestrictionH2.builder().mongoId(dr.getId()).build());
            h2.setChildId(dr.getChildId());
            h2.setType(dr.getType());
            h2.setAllergen(dr.getAllergen());
            h2.setSeverity(dr.getSeverity());
            h2.setDescription(dr.getDescription());
            h2.setNotes(dr.getNotes());
            h2.setCreatedAt(dr.getCreatedAt());
            dietaryH2Repo.save(h2);
            log.debug("[H2-Backup] DietaryRestriction saved: {}", dr.getId());
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to backup DietaryRestriction {}: {}", dr.getId(), ex.getMessage());
        }
    }

    @Async
    @Transactional("h2TransactionManager")
    public void deleteDietaryRestriction(String mongoId) {
        try {
            dietaryH2Repo.findByMongoId(mongoId).ifPresent(d -> {
                dietaryH2Repo.delete(d);
                log.debug("[H2-Backup] DietaryRestriction deleted: {}", mongoId);
            });
        } catch (Exception ex) {
            log.error("[H2-Backup] Failed to delete DietaryRestriction {}: {}", mongoId, ex.getMessage());
        }
    }
}

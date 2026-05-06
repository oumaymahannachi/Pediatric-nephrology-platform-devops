package tn.pedialink.treatment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.treatment.dto.ApiMessage;
import tn.pedialink.treatment.entity.*;
import tn.pedialink.treatment.service.AppointmentService;
import tn.pedialink.treatment.service.ChildService;
import tn.pedialink.treatment.service.GrowthService;
import tn.pedialink.treatment.service.NutritionalPlanService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final ChildService childService;
    private final GrowthService growthService;
    private final AppointmentService appointmentService;
    private final NutritionalPlanService nutritionalPlanService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(Authentication auth) {
        String doctorId = auth.getName();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("patientsCount", childService.getChildrenByDoctor(doctorId).size());
        data.put("appointmentsCount", appointmentService.countByDoctor(doctorId));
        data.put("pendingAppointmentsCount", appointmentService.countPendingByDoctor(doctorId));
        data.put("nutritionalPlansCount", nutritionalPlanService.countByDoctor(doctorId));
        return ResponseEntity.ok(data);
    }

    @GetMapping("/patients")
    public ResponseEntity<List<Map<String, Object>>> getPatients(Authentication auth) {
        String doctorId = auth.getName();
        List<Child> children = childService.getChildrenByDoctor(doctorId);
        List<Map<String, Object>> patients = children.stream().map(c -> {
            Map<String, Object> p = new LinkedHashMap<>();
            p.put("id", c.getId());
            p.put("fullName", c.getFullName());
            p.put("dateOfBirth", c.getDateOfBirth());
            p.put("gender", c.getGender());
            p.put("parentId", c.getParentId());
            p.put("doctorIds", c.getDoctorIds());
            p.put("notes", c.getNotes());
            return p;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/patients/{childId}/measurements")
    public ResponseEntity<List<GrowthMeasurement>> getPatientMeasurements(@PathVariable String childId) {
        return ResponseEntity.ok(growthService.getMeasurements(childId));
    }

    @GetMapping("/patients/{childId}/restrictions")
    public ResponseEntity<List<DietaryRestriction>> getPatientRestrictions(@PathVariable String childId) {
        return ResponseEntity.ok(growthService.getRestrictions(childId));
    }

    @GetMapping("/patients/{childId}/plans")
    public ResponseEntity<List<NutritionalPlan>> getPatientPlans(@PathVariable String childId) {
        return ResponseEntity.ok(nutritionalPlanService.getPlansByChild(childId));
    }

    @GetMapping("/nutritional-plans")
    public ResponseEntity<List<NutritionalPlan>> getMyPlans(Authentication auth) {
        return ResponseEntity.ok(nutritionalPlanService.getPlansByDoctor(auth.getName()));
    }

    @PostMapping("/nutritional-plans")
    public ResponseEntity<NutritionalPlan> createPlan(Authentication auth, @RequestBody NutritionalPlan plan) {
        return ResponseEntity.ok(nutritionalPlanService.createPlan(auth.getName(), plan));
    }

    @PutMapping("/nutritional-plans/{planId}")
    public ResponseEntity<NutritionalPlan> updatePlan(Authentication auth,
                                                       @PathVariable String planId,
                                                       @RequestBody NutritionalPlan plan) {
        return ResponseEntity.ok(nutritionalPlanService.updatePlan(auth.getName(), planId, plan));
    }

    @DeleteMapping("/nutritional-plans/{planId}")
    public ResponseEntity<ApiMessage> deletePlan(Authentication auth, @PathVariable String planId) {
        nutritionalPlanService.deletePlan(auth.getName(), planId);
        return ResponseEntity.ok(new ApiMessage("Plan deleted"));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAppointments(Authentication auth) {
        return ResponseEntity.ok(appointmentService.getByDoctor(auth.getName()));
    }

    @GetMapping("/appointments/pending")
    public ResponseEntity<List<Appointment>> getPendingAppointments(Authentication auth) {
        return ResponseEntity.ok(appointmentService.getPendingByDoctor(auth.getName()));
    }

    @PutMapping("/appointments/{id}/accept")
    public ResponseEntity<Appointment> acceptAppointment(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(appointmentService.acceptAppointment(auth.getName(), id));
    }

    @PutMapping("/appointments/{id}/refuse")
    public ResponseEntity<Appointment> refuseAppointment(Authentication auth,
                                                          @PathVariable String id,
                                                          @RequestBody(required = false) Map<String, String> body) {
        return ResponseEntity.ok(appointmentService.refuseAppointment(auth.getName(), id, body));
    }

    @PutMapping("/appointments/{id}/reschedule")
    public ResponseEntity<Appointment> rescheduleAppointment(Authentication auth,
                                                              @PathVariable String id,
                                                              @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(auth.getName(), id, body));
    }

    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<Appointment> completeAppointment(Authentication auth, @PathVariable String id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(auth.getName(), id));
    }
}

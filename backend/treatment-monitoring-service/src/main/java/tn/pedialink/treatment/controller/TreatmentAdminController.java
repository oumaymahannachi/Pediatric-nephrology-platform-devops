package tn.pedialink.treatment.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.pedialink.treatment.entity.*;
import tn.pedialink.treatment.service.AppointmentService;
import tn.pedialink.treatment.service.ChildService;
import tn.pedialink.treatment.service.GrowthService;
import tn.pedialink.treatment.service.NutritionalPlanService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/treatment-admin")
@RequiredArgsConstructor
public class TreatmentAdminController {

    private final ChildService childService;
    private final GrowthService growthService;
    private final AppointmentService appointmentService;
    private final NutritionalPlanService nutritionalPlanService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("childrenCount", childService.countAll());
        data.put("appointmentsCount", appointmentService.countAll());
        data.put("measurementsCount", growthService.countAllMeasurements());
        data.put("nutritionalPlansCount", nutritionalPlanService.countAll());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/children")
    public ResponseEntity<List<Child>> getAllChildren() {
        return ResponseEntity.ok(childService.getAllChildren());
    }

    @GetMapping("/children/{childId}/measurements")
    public ResponseEntity<List<GrowthMeasurement>> getChildMeasurements(@PathVariable String childId) {
        return ResponseEntity.ok(growthService.getMeasurements(childId));
    }

    @GetMapping("/children/{childId}/restrictions")
    public ResponseEntity<List<DietaryRestriction>> getChildRestrictions(@PathVariable String childId) {
        return ResponseEntity.ok(growthService.getRestrictions(childId));
    }

    @GetMapping("/children/{childId}/plans")
    public ResponseEntity<List<NutritionalPlan>> getChildPlans(@PathVariable String childId) {
        return ResponseEntity.ok(nutritionalPlanService.getPlansByChild(childId));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    @GetMapping("/nutritional-plans")
    public ResponseEntity<List<NutritionalPlan>> getAllPlans() {
        return ResponseEntity.ok(nutritionalPlanService.getAllPlans());
    }

    @GetMapping("/measurements")
    public ResponseEntity<List<GrowthMeasurement>> getAllMeasurements() {
        return ResponseEntity.ok(growthService.getAllMeasurements());
    }

    @GetMapping("/restrictions")
    public ResponseEntity<List<DietaryRestriction>> getAllRestrictions() {
        return ResponseEntity.ok(growthService.getAllRestrictions());
    }
}

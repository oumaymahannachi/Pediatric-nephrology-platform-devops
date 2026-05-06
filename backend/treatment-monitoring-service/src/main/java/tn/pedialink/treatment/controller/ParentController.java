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

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parent")
@RequiredArgsConstructor
public class ParentController {

    private final ChildService childService;
    private final GrowthService growthService;
    private final AppointmentService appointmentService;
    private final NutritionalPlanService nutritionalPlanService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(Authentication auth) {
        String parentId = auth.getName();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("childrenCount", childService.countByParent(parentId));
        data.put("appointmentsCount", appointmentService.countByParent(parentId));
        return ResponseEntity.ok(data);
    }

    @GetMapping("/children")
    public ResponseEntity<List<Child>> getChildren(Authentication auth) {
        return ResponseEntity.ok(childService.getChildrenByParent(auth.getName()));
    }

    @PostMapping("/children")
    public ResponseEntity<Child> addChild(Authentication auth, @RequestBody Child child) {
        return ResponseEntity.ok(childService.addChild(auth.getName(), child));
    }

    @PutMapping("/children/{id}")
    public ResponseEntity<Child> updateChild(Authentication auth, @PathVariable String id, @RequestBody Child child) {
        return ResponseEntity.ok(childService.updateChild(auth.getName(), id, child));
    }

    @DeleteMapping("/children/{id}")
    public ResponseEntity<ApiMessage> deleteChild(Authentication auth, @PathVariable String id) {
        childService.deleteChild(auth.getName(), id);
        return ResponseEntity.ok(new ApiMessage("Child deleted"));
    }

    @PostMapping("/children/{childId}/doctors/{doctorId}")
    public ResponseEntity<Child> assignDoctor(Authentication auth,
                                               @PathVariable String childId,
                                               @PathVariable String doctorId) {
        return ResponseEntity.ok(childService.assignDoctor(auth.getName(), childId, doctorId));
    }

    @DeleteMapping("/children/{childId}/doctors/{doctorId}")
    public ResponseEntity<Child> removeDoctor(Authentication auth,
                                               @PathVariable String childId,
                                               @PathVariable String doctorId) {
        return ResponseEntity.ok(childService.removeDoctor(auth.getName(), childId, doctorId));
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<Map<String, String>>> getAvailableDoctors() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/children/{childId}/measurements")
    public ResponseEntity<List<GrowthMeasurement>> getMeasurements(@PathVariable String childId) {
        return ResponseEntity.ok(growthService.getMeasurements(childId));
    }

    @PostMapping("/children/{childId}/measurements")
    public ResponseEntity<GrowthMeasurement> addMeasurement(Authentication auth,
                                                             @PathVariable String childId,
                                                             @RequestBody GrowthMeasurement measurement) {
        measurement.setChildId(childId);
        measurement.setRecordedBy(auth.getName());
        if (measurement.getDate() == null) {
            measurement.setDate(LocalDate.now());
        }
        return ResponseEntity.ok(growthService.addMeasurement(measurement));
    }

    @PutMapping("/children/{childId}/measurements/{measurementId}")
    public ResponseEntity<GrowthMeasurement> updateMeasurement(@PathVariable String childId,
                                                                @PathVariable String measurementId,
                                                                @RequestBody GrowthMeasurement measurement) {
        return ResponseEntity.ok(growthService.updateMeasurement(measurementId, measurement));
    }

    @DeleteMapping("/children/{childId}/measurements/{measurementId}")
    public ResponseEntity<ApiMessage> deleteMeasurement(@PathVariable String childId,
                                                         @PathVariable String measurementId) {
        growthService.deleteMeasurement(measurementId);
        return ResponseEntity.ok(new ApiMessage("Measurement deleted"));
    }

    @GetMapping("/children/{childId}/restrictions")
    public ResponseEntity<List<DietaryRestriction>> getRestrictions(@PathVariable String childId) {
        return ResponseEntity.ok(growthService.getRestrictions(childId));
    }

    @PostMapping("/children/{childId}/restrictions")
    public ResponseEntity<DietaryRestriction> addRestriction(@PathVariable String childId,
                                                              @RequestBody DietaryRestriction restriction) {
        restriction.setChildId(childId);
        return ResponseEntity.ok(growthService.addRestriction(restriction));
    }

    @PutMapping("/children/{childId}/restrictions/{restrictionId}")
    public ResponseEntity<DietaryRestriction> updateRestriction(@PathVariable String childId,
                                                                 @PathVariable String restrictionId,
                                                                 @RequestBody DietaryRestriction restriction) {
        return ResponseEntity.ok(growthService.updateRestriction(restrictionId, restriction));
    }

    @DeleteMapping("/children/{childId}/restrictions/{restrictionId}")
    public ResponseEntity<ApiMessage> deleteRestriction(@PathVariable String childId,
                                                         @PathVariable String restrictionId) {
        growthService.deleteRestriction(restrictionId);
        return ResponseEntity.ok(new ApiMessage("Restriction deleted"));
    }

    @GetMapping("/children/{childId}/plans")
    public ResponseEntity<List<NutritionalPlan>> getChildPlans(@PathVariable String childId) {
        return ResponseEntity.ok(nutritionalPlanService.getPlansByChild(childId));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<Appointment>> getAppointments(Authentication auth) {
        return ResponseEntity.ok(appointmentService.getByParent(auth.getName()));
    }

    @PostMapping("/appointments")
    public ResponseEntity<Appointment> createAppointment(Authentication auth, @RequestBody Appointment appointment) {
        return ResponseEntity.ok(appointmentService.createAppointment(auth.getName(), appointment));
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<ApiMessage> cancelAppointment(Authentication auth, @PathVariable String id) {
        appointmentService.cancelAppointment(auth.getName(), id);
        return ResponseEntity.ok(new ApiMessage("Appointment cancelled"));
    }
}

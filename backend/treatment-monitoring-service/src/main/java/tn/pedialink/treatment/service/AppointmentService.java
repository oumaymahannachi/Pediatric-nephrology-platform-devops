package tn.pedialink.treatment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.pedialink.treatment.entity.Appointment;
import tn.pedialink.treatment.entity.AppointmentStatus;
import tn.pedialink.treatment.exception.BadRequestException;
import tn.pedialink.treatment.exception.NotFoundException;
import tn.pedialink.treatment.repository.AppointmentRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepo;

    public List<Appointment> getByParent(String parentId) {
        return appointmentRepo.findByParentId(parentId);
    }

    public Appointment createAppointment(String parentId, Appointment appointment) {
        appointment.setParentId(parentId);
        appointment.setStatus(AppointmentStatus.PENDING);
        return appointmentRepo.save(appointment);
    }

    public void cancelAppointment(String parentId, String appointmentId) {
        Appointment apt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        if (!apt.getParentId().equals(parentId)) {
            throw new BadRequestException("Not authorized");
        }
        apt.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepo.save(apt);
    }

    public List<Appointment> getByDoctor(String doctorId) {
        return appointmentRepo.findByDoctorId(doctorId);
    }

    public List<Appointment> getPendingByDoctor(String doctorId) {
        return appointmentRepo.findByDoctorIdAndStatus(doctorId, AppointmentStatus.PENDING);
    }

    public Appointment acceptAppointment(String doctorId, String appointmentId) {
        Appointment apt = getAndValidateDoctor(doctorId, appointmentId);
        apt.setStatus(AppointmentStatus.ACCEPTED);
        return appointmentRepo.save(apt);
    }

    public Appointment refuseAppointment(String doctorId, String appointmentId, Map<String, String> body) {
        Appointment apt = getAndValidateDoctor(doctorId, appointmentId);
        apt.setStatus(AppointmentStatus.REFUSED);
        if (body != null && body.containsKey("doctorNotes")) {
            apt.setDoctorNotes(body.get("doctorNotes"));
        }
        return appointmentRepo.save(apt);
    }

    public Appointment rescheduleAppointment(String doctorId, String appointmentId, Map<String, String> body) {
        Appointment apt = getAndValidateDoctor(doctorId, appointmentId);
        apt.setStatus(AppointmentStatus.RESCHEDULED);
        if (body != null) {
            if (body.containsKey("proposedDateTime")) {
                apt.setProposedDateTime(body.get("proposedDateTime"));
            }
            if (body.containsKey("doctorNotes")) {
                apt.setDoctorNotes(body.get("doctorNotes"));
            }
        }
        return appointmentRepo.save(apt);
    }

    public Appointment completeAppointment(String doctorId, String appointmentId) {
        Appointment apt = getAndValidateDoctor(doctorId, appointmentId);
        apt.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepo.save(apt);
    }

    public long countByParent(String parentId) {
        return appointmentRepo.countByParentId(parentId);
    }

    public long countByDoctor(String doctorId) {
        return appointmentRepo.countByDoctorId(doctorId);
    }

    public long countPendingByDoctor(String doctorId) {
        return appointmentRepo.countByDoctorIdAndStatus(doctorId, AppointmentStatus.PENDING);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public long countAll() {
        return appointmentRepo.count();
    }

    private Appointment getAndValidateDoctor(String doctorId, String appointmentId) {
        Appointment apt = appointmentRepo.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));
        if (!apt.getDoctorId().equals(doctorId)) {
            throw new BadRequestException("Not authorized");
        }
        return apt;
    }
}

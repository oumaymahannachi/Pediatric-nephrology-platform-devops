package tn.pedialink.treatment.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.treatment.entity.Appointment;
import tn.pedialink.treatment.entity.AppointmentStatus;

import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {

    List<Appointment> findByParentId(String parentId);

    List<Appointment> findByDoctorId(String doctorId);

    List<Appointment> findByDoctorIdAndStatus(String doctorId, AppointmentStatus status);

    long countByParentId(String parentId);

    long countByDoctorId(String doctorId);

    long countByDoctorIdAndStatus(String doctorId, AppointmentStatus status);
}

package tn.pedialink.treatment.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.treatment.h2.entity.AppointmentH2;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentH2Repository extends JpaRepository<AppointmentH2, String> {
    Optional<AppointmentH2> findByMongoId(String mongoId);
    List<AppointmentH2> findByChildId(String childId);
    List<AppointmentH2> findByDoctorId(String doctorId);
    List<AppointmentH2> findByParentId(String parentId);
    List<AppointmentH2> findByStatus(String status);
}

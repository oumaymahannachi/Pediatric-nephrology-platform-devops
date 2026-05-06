package tn.pedialink.labresults.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.labresults.entity.LabResult;
import tn.pedialink.labresults.entity.TestType;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LabResultRepository extends MongoRepository<LabResult, String> {
    
    List<LabResult> findByPatientIdOrderByTestDateDesc(String patientId);
    
    List<LabResult> findByPatientIdAndTestTypeOrderByTestDateDesc(String patientId, TestType testType);
    
    List<LabResult> findByPatientIdAndIsAbnormalTrueOrderByTestDateDesc(String patientId);
    
    List<LabResult> findByPatientIdAndTestDateBetweenOrderByTestDateDesc(
            String patientId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<LabResult> findByDoctorIdOrderByTestDateDesc(String doctorId);
}

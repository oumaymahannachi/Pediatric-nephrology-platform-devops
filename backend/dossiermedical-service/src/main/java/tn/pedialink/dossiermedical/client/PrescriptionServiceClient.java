package tn.pedialink.dossiermedical.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * Feign client pour communiquer avec prescription-service
 */
@FeignClient(name = "prescription-service")
public interface PrescriptionServiceClient {

    @GetMapping("/api/prescriptions/patient/{patientId}")
    List<PrescriptionDto> getPrescriptionsByPatient(@PathVariable("patientId") String patientId);
}

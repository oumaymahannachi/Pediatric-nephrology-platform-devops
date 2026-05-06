package tn.pedialink.prescription.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import tn.pedialink.prescription.model.TraitementChronique;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TraitementChroniqueRepository extends MongoRepository<TraitementChronique, String> {

    List<TraitementChronique> findByPatientIdAndActifTrue(String patientId);

    List<TraitementChronique> findByMedecinPrescripteurId(String medecinId);

    @Query("{ 'actif': true, 'dateProchainRenouvellement': { $gte: ?0, $lte: ?1 } }")
    List<TraitementChronique> findNeedingRenewalBetween(LocalDate start, LocalDate end);

    @Query("{ 'actif': true, 'bilansProgrammes.datePrevue': { $gte: ?0, $lte: ?1 }, 'bilansProgrammes.realise': false }")
    List<TraitementChronique> findUpcomingBilans(LocalDate start, LocalDate end);

    Optional<TraitementChronique> findByIdAndPatientId(String id, String patientId);
}
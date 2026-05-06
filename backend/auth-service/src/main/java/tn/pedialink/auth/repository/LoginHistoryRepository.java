package tn.pedialink.auth.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.pedialink.auth.entity.LoginHistory;

import java.time.Instant;
import java.util.List;

public interface LoginHistoryRepository extends MongoRepository<LoginHistory, String> {
    List<LoginHistory> findAllByUserIdOrderByTimestampDesc(String userId);
    List<LoginHistory> findAllByEmailOrderByTimestampDesc(String email);
    List<LoginHistory> findAllByOrderByTimestampDesc();
    List<LoginHistory> findAllBySuccessFalseOrderByTimestampDesc();
    List<LoginHistory> findAllByTimestampBetweenOrderByTimestampDesc(Instant from, Instant to);
    long countByUserId(String userId);
    long countBySuccessFalse();
}

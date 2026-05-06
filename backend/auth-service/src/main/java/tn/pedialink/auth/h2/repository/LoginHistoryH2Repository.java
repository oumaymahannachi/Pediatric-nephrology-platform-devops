package tn.pedialink.auth.h2.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.pedialink.auth.h2.entity.LoginHistoryH2;

import java.util.List;

@Repository
public interface LoginHistoryH2Repository extends JpaRepository<LoginHistoryH2, String> {

    List<LoginHistoryH2> findByUserIdOrderByTimestampDesc(String userId);

    long countByUserId(String userId);
}

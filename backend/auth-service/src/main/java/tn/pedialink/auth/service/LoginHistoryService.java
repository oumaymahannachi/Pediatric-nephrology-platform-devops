package tn.pedialink.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.pedialink.auth.dto.LoginHistoryDto;
import tn.pedialink.auth.entity.LoginHistory;
import tn.pedialink.auth.repository.LoginHistoryRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    public List<LoginHistoryDto> getAllLoginHistory() {
        return loginHistoryRepository.findAllByOrderByTimestampDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<LoginHistoryDto> getLoginHistoryByUser(String userId) {
        return loginHistoryRepository.findAllByUserIdOrderByTimestampDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<LoginHistoryDto> getFailedAttempts() {
        return loginHistoryRepository.findAllBySuccessFalseOrderByTimestampDesc()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<LoginHistoryDto> getRecentLoginHistory(int hours) {
        Instant from = Instant.now().minus(hours, ChronoUnit.HOURS);
        Instant to = Instant.now();
        return loginHistoryRepository.findAllByTimestampBetweenOrderByTimestampDesc(from, to)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public long countTotalLogins() {
        return loginHistoryRepository.count();
    }

    public long countFailedAttempts() {
        return loginHistoryRepository.countBySuccessFalse();
    }

    private LoginHistoryDto toDto(LoginHistory h) {
        return LoginHistoryDto.builder()
                .id(h.getId())
                .userId(h.getUserId())
                .email(h.getEmail())
                .ipAddress(h.getIpAddress())
                .userAgent(h.getUserAgent())
                .success(h.isSuccess())
                .failureReason(h.getFailureReason())
                .role(h.getRole() != null ? h.getRole().name() : null)
                .timestamp(h.getTimestamp())
                .build();
    }
}

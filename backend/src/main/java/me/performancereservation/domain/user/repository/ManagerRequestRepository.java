package me.performancereservation.domain.user.repository;

import me.performancereservation.domain.user.entitiy.ManagerRequest;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ManagerRequestRepository extends JpaRepository<ManagerRequest, Long> {
    // 해당 상태의 요청 존재 여부 확인
    boolean existsByUserIdAndStatus(Long userId, ManagerRequestStatus status);

    // PENDING 상태의 요청 존재 여부 확인
    default boolean hasPendingRequest(Long userId) {
        return existsByUserIdAndStatus(userId, ManagerRequestStatus.PENDING);
    }

    // APPROVED 상태의 요청 존재 여부 확인
    default boolean hasApprovedRequest(Long userId) {
        return existsByUserIdAndStatus(userId, ManagerRequestStatus.APPROVED);
    }
}

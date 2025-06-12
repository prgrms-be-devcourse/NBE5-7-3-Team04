package me.performancereservation.domain.admin.repository;

import me.performancereservation.domain.user.entity.ManagerRequest;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminManagerRequestRepository extends JpaRepository<ManagerRequest, Long> {
    Page<ManagerRequest> findAllByStatusOrderByCreatedAt(ManagerRequestStatus status, Pageable pageable);
}

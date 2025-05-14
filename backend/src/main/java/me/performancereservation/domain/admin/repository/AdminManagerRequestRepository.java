package me.performancereservation.domain.admin.repository;

import me.performancereservation.domain.user.entitiy.ManagerRequest;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminManagerRequestRepository extends JpaRepository<ManagerRequest, Long> {
    Page<ManagerRequest> findAllByStatusOrderByCreatedAt(ManagerRequestStatus status, Pageable pageable);
}

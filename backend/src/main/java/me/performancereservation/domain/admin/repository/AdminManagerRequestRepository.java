package me.performancereservation.domain.admin.repository;

import me.performancereservation.domain.user.entitiy.ManagerRequest;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminManagerRequestRepository extends JpaRepository<ManagerRequest, Long> {
    /** 관리자가 상태를 이용해 공연 관리자 신청 목록을 조회하는 쿼리
     *
     * ManagerRequest 테이블에서 받은 상태에 해당하는 공연 관리자 신청 목록을 조회
     * 최신순 출력을 위해 생성 시간 내림차순 정렬
     * @param pageable
     * @return Page<ManagerRequest>
     */
//    @Query("""
//        SELECT m
//        FROM ManagerRequest m
//        WHERE m.status = :status
//        ORDER BY m.createdAt DESC
//    """)
//    Page<ManagerRequest> findPendingManagerRequests(@Param("status") ManagerRequestStatus status, Pageable pageable);

    Page<ManagerRequest> findAllByStatusOrderByCreatedAt(ManagerRequestStatus status, Pageable pageable);
}

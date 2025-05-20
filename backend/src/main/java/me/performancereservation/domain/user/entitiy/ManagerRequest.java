package me.performancereservation.domain.user.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.common.BaseEntity;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;

import java.time.LocalDate;


@Entity
@Getter
@NoArgsConstructor
public class ManagerRequest extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 매니저 권한 승인 요청 내역 ID

    private Long userId; // (FK) 유저 ID

    private String organizationName;        // 개인 혹은 단체명
    private String organizationContact;     // 연락처
    private String experience;              // 공연 경험
    private String reason;                  // 신청 사유

    @Enumerated(EnumType.STRING)
    private ManagerRequestStatus status; // 승인 요청 상태

    private LocalDate approvedAt; // 승인 날짜

    @Builder
    public ManagerRequest(Long id,
                          Long userId,
                          String reason,
                          String experience,
                          LocalDate approvedAt,
                          String organizationName,
                          String organizationContact,
                          ManagerRequestStatus status) {
        this.id = id;
        this.userId = userId;
        this.status = status;
        this.reason = reason;
        this.approvedAt = approvedAt;
        this.experience = experience;
        this.organizationName = organizationName;
        this.organizationContact = organizationContact;
    }

    public boolean isPending() {
        return status == ManagerRequestStatus.PENDING;
    }

    public void approve() {
        status = ManagerRequestStatus.APPROVED;
        approvedAt = LocalDate.now();
    }

    public void reject() {
        status = ManagerRequestStatus.REJECTED;
    }
}

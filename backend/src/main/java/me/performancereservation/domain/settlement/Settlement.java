package me.performancereservation.domain.settlement;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.common.BaseEntity;
import me.performancereservation.domain.settlement.enums.SettlementStatus;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Settlement extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 정산 ID

    private Long performanceId; // 공연 ID

    private int totalAmount; // 총 정산 금액

    private String account; // 계좌번호

    private String bank; // 은행명

    @Enumerated(EnumType.STRING)
    private SettlementStatus status; // 정산 상태

    private LocalDateTime settledAt; // 정산완료일시. 생성 시 초기값 null

    @Builder
    public Settlement(Long id, Long performanceId, int totalAmount, String account, String bank, SettlementStatus status) {
        this.id = id;
        this.performanceId = performanceId;
        this.totalAmount = totalAmount;
        this.account = account;
        this.bank = bank;
        this.status = status;
    }

    public void confirm(){
        this.settledAt = LocalDateTime.now();
        this.status = SettlementStatus.CONFIRMED;
    }
}

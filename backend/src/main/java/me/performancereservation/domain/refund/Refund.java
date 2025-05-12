package me.performancereservation.domain.refund;

import jakarta.persistence.*;
import lombok.*;
import me.performancereservation.domain.common.BaseEntity;
import me.performancereservation.domain.refund.enums.RefundStatus;
import me.performancereservation.global.exception.ErrorCode;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 환불요청정보 id

    private Long reservationId; // (FK) 환불을 요청하는 예약 id

    private Long userId; // (FK) 환불을 요청한 유저의 id

    private String account; // 환불 받을 계좌번호

    private String bank; // 환불 받을 은행

    private String accountOwner; // 예금주

    @Setter
    @Enumerated(EnumType.STRING)
    private RefundStatus status; // 환불대기, 환불완료 상태 표시

    public void updateBankInfo(String account, String bank, String accountOwner){
        this.account = account;
        this.bank = bank;
        this.accountOwner = accountOwner;
    }

    public void ready() {
        if (this.status == RefundStatus.READY) {
            throw ErrorCode.INVALID_REFUND_STATUS.domainException("이미 환불 준비된 상태입니다.");
        }
        this.status = RefundStatus.READY;
    }

    public void confirm() {
        if (this.status == RefundStatus.CONFIRMED) {
            throw ErrorCode.INVALID_REFUND_STATUS.domainException("이미 환불이 완료된 상태입니다.");
        }
        this.status = RefundStatus.CONFIRMED;
    }

    @Builder
    public Refund(Long id, Long reservationId, Long userId, RefundStatus status) {
        // account, bank, accountOwner는 처음 생성할 때 null
        this.id = id;
        this.reservationId = reservationId;
        this.userId = userId;
        this.status = status;
    }
}

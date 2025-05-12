package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.refund.RefundService;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.domain.refund.dto.UpdateBankInfoRequest;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/refunds")
public class RefundController {

    private final RefundService refundService;

    /*--- USER 요청에 대응 ---*/
    /// 본인 id와 일치하는 모든 환불내역 리스트 반환
    @GetMapping("/me/{userId}")
    public ResponseEntity<Page<RefundDetailResponse>> getAllRefundDetailsWithUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    // 나중에 로그인 구현 후 security로 userid 받아오는 방식으로 수정
//        Long userId = ((CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();

        log.info("사용자 환불 내역 조회 요청: userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(refundService.findAllRefundsDetailByUserId(userId, pageable));
    }

    ///  계좌, 은행명, 입금자명을 dto로 받아서 설정
    @PatchMapping
    public ResponseEntity<Void> updateBankInfo(@RequestBody UpdateBankInfoRequest request) {
        refundService.updateBankInfo(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /*--- ADMIN 요청에 대응 ---*/
    /// 모든 환불 내역 중 특정 status 리스트 반환
    /// /refunds/admin?status=CONFIRMED
    @GetMapping("/admin")
    public ResponseEntity<Page<RefundDetailResponse>> getAllRefundDetailsByRefundStatus(
            @RequestParam(required = false) String refundStatus,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("상태별 환불 내역 조회 요청: status={}, page={}, size={}", 
                refundStatus, pageable.getPageNumber(), pageable.getPageSize());

        // 쿼리 파라미터로 status가 지정되지 않았을 경우 전체 조회
        if (refundStatus == null) {
            return ResponseEntity.ok(refundService.findAllRefundsDetail(pageable));
        }

        // status로 조회. 유효성 검사는 서비스측에서 수행
        return ResponseEntity.status(HttpStatus.OK)
                .body(refundService.findAllRefundsDetailByRefundStatus(refundStatus, pageable));
    }

    /// 환불 상태 변경 (환불 승인) PENDING-> CONFIRMED
    /// updateRefundStatus 내부에서 예약 상태도 변경됨 (CANCEL_PENDING-> CANCEL_CONFIRMED)
    @PatchMapping("/admin/{refundId}/confirm")
    public ResponseEntity<Void> confirmRefund(@PathVariable Long refundId) {
        log.info("환불 승인 요청: refundId={}", refundId);
        
        refundService.confirmRefund(refundId);
        return ResponseEntity.noContent().build();
    }

    ///  예: refunds/3?status=pending
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateRefundStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        if (status == null) {
            throw ErrorCode.INVALID_REFUND_STATUS.domainException("변경할 환불 상태를 입력하지 않았습니다.");
        }
        refundService.updateRefundStatus(id, status);
        return ResponseEntity.ok().build();
    }
}

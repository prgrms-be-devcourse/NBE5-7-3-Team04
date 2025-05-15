package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.refund.RefundService;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.domain.refund.dto.UpdateBankInfoRequest;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/refunds")
public class RefundController {

    private final RefundService refundService;

    /*--- USER 요청에 대응 ---*/
    /// 본인 id와 일치하는 모든 환불내역 리스트 반환
    @GetMapping("/me")
    public ResponseEntity<Page<RefundDetailResponse>> getAllRefundDetailsWithUserId(
            @AuthenticationPrincipal CustomOAuth2User authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = authentication.getUser().getId();

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
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("상태별 환불 내역 조회 요청: status={}, page={}, size={}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        // status로 조회. 유효성 검사는 서비스측에서 수행
        // status가 null일 경우 전체조회 응답 받음
        return ResponseEntity.ok(refundService.findAllRefundsDetail(status, pageable));
    }

    /// 환불 상태 변경 (환불 승인) PENDING-> CONFIRMED
    /// updateRefundStatus 내부에서 예약 상태도 변경됨 (CANCEL_PENDING-> CANCEL_CONFIRMED)
    @PatchMapping("/admin/{refundId}/confirm")
    public ResponseEntity<Void> confirmRefund(@PathVariable Long refundId) {
        log.info("환불 승인 요청: refundId={}", refundId);
        
        refundService.confirmRefund(refundId);
        return ResponseEntity.noContent().build();
    }

}

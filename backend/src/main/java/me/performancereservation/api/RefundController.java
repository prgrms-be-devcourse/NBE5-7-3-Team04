package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.api.docs.RefundApiDocs;
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
public class RefundController implements RefundApiDocs {

    private final RefundService refundService;

    /*--- USER 요청에 대응 ---*/

    /**
     * 본인 id와 일치하는 모든 환불내역 리스트 반환.
     * /refunds/me
     * @param authentication 현재 로그인한 인증 정보
     * @param pageable 기본값
     * @return ResponseEntity<Page<RefundDetailResponse>>
     */
    @Override
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

    /**
     * 계좌, 은행명, 입금자명을 dto로 받아서 설정.
     * /refunds
     * @param request
     * @return
     */
    @Override
    @PatchMapping
    public ResponseEntity<Void> updateBankInfo(
            @AuthenticationPrincipal CustomOAuth2User authentication,
            @RequestBody UpdateBankInfoRequest request) {

        Long userId = authentication.getUser().getId();

        refundService.updateBankInfo(userId, request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}

package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.settlement.SettlementService;
import me.performancereservation.domain.settlement.dto.SettlementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/settlements")
@RequiredArgsConstructor
public class AdminSettlementController {

    private final SettlementService settlementService;

    /**
     * api/v1/admin/settlements/{settlementId}/confirm 으로 ADMIN이 승인진행
     * @param settlementId 승인할 정산 id
     * @return ResponseEntity<SettlementResponse> 필드 변경된 정산 결과
     */
    @PatchMapping("/{settlementId}/confirm")
    public ResponseEntity<SettlementResponse> confirmSettlement(@PathVariable Long settlementId) {
        SettlementResponse response = settlementService.confirmSettlement(settlementId);
        return ResponseEntity.ok(response);
    }


    /**
     * api/v1/admin/settlements?status=__ 으로 모든 유저의 정산 조회.
     * status == null이면 전체 조회.
     * status가 유효하면 해당 status인 정산만 조회.
     * @param status PENDING 혹은 CONFIRMED여야 한다
     * @return ResponseEntity<Page<SettlementResponse>> status로 필터링한 모든 유저의 정산목록 Page
     */
    @GetMapping()
    public ResponseEntity<Page<SettlementResponse>> getAllSettlementsWithStatus(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("ADMIN 정산 내역 조회 요청: status={}, page={}, size={}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        Page<SettlementResponse> response;

        // 서비스 내에서 status string 유효성 검사 진행
        response = settlementService.findAllSettlementsByStatus(status, pageable);

        return ResponseEntity.ok(response);
    }
}

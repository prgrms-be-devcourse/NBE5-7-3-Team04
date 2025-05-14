package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.settlement.SettlementService;
import me.performancereservation.domain.settlement.dto.SettlementRequest;
import me.performancereservation.domain.settlement.dto.SettlementResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * api/v1/settlements로 공연관리자가 정산생성 요청
     * @param request SettlementRequest DTO
     * @return 생성된 정산 id
     */
    @PostMapping
    public ResponseEntity<Long> createSettlement(@RequestBody SettlementRequest request) {
        Long settlementId = settlementService.createSettlement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementId);
    }

    /**
     * api/v1/settlements/admin/{settlementId}/confirm으로 ADMIN이 승인진행
     * @param settlementId 승인할 정산 id
     * @return 필드 변경된 정산 결과
     */
    @PatchMapping("/admin/{settlementId}/confirm")
    public ResponseEntity<SettlementResponse> confirmSettlement(@PathVariable Long settlementId) {
        SettlementResponse response = settlementService.confirmSettlement(settlementId);
        return ResponseEntity.ok(response);
    }

    // 본인 userid로 전체 정산목록 조회 → return Page<SettlementResponse >

    /**
     * 현재 로그인된 userid로 정산목록 조회
     * @param userId 현재 로그인된 userid
     * @param pageable 조회할 페이지
     * @return 유저id로 조회한 정산목록 Page
     */
    @GetMapping("/me/{userId}") // TODO: 로그인 구현 후 userId는 security로 받아오도록 수정
    public ResponseEntity<Page<SettlementResponse>> getAllSettlementsWithUserId(
            @PathVariable Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        log.info("사용자 정산 내역 조회 요청: userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        return ResponseEntity.ok(settlementService.findAllSettlementsWithUserId(userId, pageable));
    }

    /**
     * api/v1/settlements/admin으로 모든 유저의 정산 조회.
     * status == null이면 전체 조회.
     * status가 유효하면 해당 status인 정산만 조회.
     * @param status PENDING 혹은 CONFIRMED여야 한다
     * @return status로 필터링한 모든 유저의 정산목록 Page
     */
    @GetMapping("/admin")
    public ResponseEntity<Page<SettlementResponse>> getAllSettlementsWithStatus(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("관리자 정산 내역 조회 요청: status={}, page={}, size={}",
                status, pageable.getPageNumber(), pageable.getPageSize());

        Page<SettlementResponse> response;

        // 서비스 내에서 status string 유효성 검사 진행
        response = settlementService.findAllSettlementsByStatus(status, pageable);

        return ResponseEntity.ok(response);
    }
}

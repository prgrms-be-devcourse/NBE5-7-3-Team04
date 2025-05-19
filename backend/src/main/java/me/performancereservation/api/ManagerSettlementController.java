package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.api.docs.SettlementApiDocs;
import me.performancereservation.domain.settlement.SettlementService;
import me.performancereservation.domain.settlement.dto.SettlementRequest;
import me.performancereservation.domain.settlement.dto.SettlementResponse;
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
@RequestMapping("/api/v1/managers/settlements")
@RequiredArgsConstructor
public class ManagerSettlementController implements SettlementApiDocs {

    private final SettlementService settlementService;

    /**
     * api/v1/managers/settlements/register 로 공연관리자가 정산생성 요청
     * @param request SettlementRequest DTO
     * @return 생성된 정산 Long id
     */
    @Override
    @PostMapping("/register")
    public ResponseEntity<Long> createSettlement(@RequestBody SettlementRequest request) {
        Long settlementId = settlementService.createSettlement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementId);
    }


    // 본인 userid로 전체 정산목록 조회 → return Page<SettlementResponse >

    /**
     * 현재 로그인된 userid로 정산목록 조회.
     * /api/v1/managers/settlements/me
     * @param authentication 현재 로그인 한 OAuth2User의 principal
     * @param pageable 조회할 페이지
     * @return ResponseEntity<Page<SettlementResponse>> 유저id로 조회한 정산목록 Page
     */
    @Override
    @GetMapping("/me")
    public ResponseEntity<Page<SettlementResponse>> getAllSettlementsWithUserId(
            @AuthenticationPrincipal CustomOAuth2User authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable){

        Long userId = authentication.getUser().getId();
        log.info("사용자 정산 내역 조회 요청: userId={}, page={}, size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());

        return ResponseEntity.ok(settlementService.findAllSettlementsWithUserId(userId, pageable));
    }

}

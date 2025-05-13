package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.settlement.SettlementService;
import me.performancereservation.domain.settlement.dto.SettlementRequest;
import me.performancereservation.domain.settlement.dto.SettlementResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    // api/v1/settlements
    @PostMapping
    public ResponseEntity<Long> createSettlement(@RequestBody SettlementRequest request) {
        Long settlementId = settlementService.createSettlement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(settlementId);
    }

    // api/v1/settlements/admin/{settlementId}/confirm
    // refund의 confirm 엔드포인트와 형식 맞춰줌
    @PatchMapping("/admin/{settlementId}/confirm")
    public ResponseEntity<SettlementResponse> confirmSettlement(@PathVariable Long settlementId) {
        SettlementResponse response = settlementService.confirmSettlement(settlementId);
        return ResponseEntity.ok(response);
    }
}

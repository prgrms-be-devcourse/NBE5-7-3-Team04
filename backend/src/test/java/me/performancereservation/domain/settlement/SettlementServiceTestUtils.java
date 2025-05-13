package me.performancereservation.domain.settlement;

import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.settlement.dto.SettlementResponse;

import java.util.List;

@Slf4j
public class SettlementServiceTestUtils {

    public static void logPerformanceInfo(Performance performance) {
        log.info("=== 공연 정보 ===");
        log.info("공연 ID: {}", performance.getId());
        log.info("공연 제목: {}", performance.getTitle());
        log.info("공연 가격: {}원", performance.getPrice());
        log.info("총 좌석 수: {}석", performance.getTotalSeats());
        log.info("================\n");
    }

    public static void logScheduleInfo(List<PerformanceSchedule> schedules) {
        log.info("=== 공연 스케줄 정보 ===");
        for (PerformanceSchedule schedule : schedules) {
            log.info("스케줄 ID: {}", schedule.getId());
            log.info("시작 시간: {}", schedule.getStartTime());
            log.info("남은 좌석: {}석", schedule.getRemainingSeats());
            log.info("취소 여부: {}", schedule.isCanceled());
            log.info("-------------------");
        }
        log.info("=====================\n");
    }

    public static void logSettlementInfo(Settlement settlement) {
        log.info("=== 정산 정보 ===");
        log.info("정산 ID: {}", settlement.getId());
        log.info("공연 ID: {}", settlement.getPerformanceId());
        log.info("총 정산 금액: {}원", settlement.getTotalAmount());
        log.info("계좌번호: {}", settlement.getAccount());
        log.info("은행: {}", settlement.getBank());
        log.info("상태: {}", settlement.getStatus());
        log.info("정산 완료 시간: {}", settlement.getSettledAt());
        log.info("================\n");
    }

    public static void logSettlementResponse(SettlementResponse response) {
        log.info("=== 정산 응답 정보 ===");
        log.info("정산 ID: {}", response.settlementId());
        log.info("총 정산 금액: {}원", response.totalAmount());
        log.info("정산 완료 시간: {}", response.settledAt());
        log.info("계좌번호: {}", response.account());
        log.info("은행: {}", response.bank());
        log.info("상태: {}", response.status());
        log.info("공연 제목: {}", response.title());
        log.info("===================\n");
    }
}

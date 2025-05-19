package me.performancereservation.domain.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.performance.service.PerformanceService;
import me.performancereservation.domain.settlement.dto.SettlementRequest;
import me.performancereservation.domain.settlement.dto.SettlementResponse;
import me.performancereservation.domain.settlement.dto.SettlementUpdateRequest;
import me.performancereservation.domain.settlement.dto.SettlementUpdateResponse;
import me.performancereservation.domain.settlement.enums.SettlementStatus;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.performancereservation.domain.performance.repository.PerformanceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {
    private final SettlementRepository settlementRepository;
    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository performanceScheduleRepository;
    private final PerformanceService performanceService;

    @Transactional
    public Long createSettlement(SettlementRequest request) {
        // 공연 정보 조회
        Performance performance = performanceRepository.findById(request.performanceId())
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("존재하지 않는 공연입니다."));

        // 공연 스케줄 조회
        List<PerformanceSchedule> schedules = performanceScheduleRepository.findByPerformanceId(performance.getId());
        log.info("불러온 스케줄 리스트: {}개", schedules != null ? schedules.size() : null);
        if (schedules != null) {
            for (PerformanceSchedule schedule : schedules) {
                log.info("schedule: id={}, startTime={}, endTime={}",
                        schedule != null ? schedule.getId() : null,
                        schedule != null ? schedule.getStartTime() : null,
                        schedule != null ? schedule.getEndTime() : null);
            }
        }
        if (schedules == null) {
            schedules = List.of(); // null이면 빈 리스트로 처리
        }

        // 가장 늦은 공연 날짜 확인 (스케쥴이 없으면 latestSchedule도 null)
        LocalDateTime latestSchedule = schedules.stream()
                .filter(java.util.Objects::nonNull)
                .map(PerformanceSchedule::getStartTime)
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // 정산 신청 가능 날짜 체크 (스케쥴이 없으면 바로 예외)
        if (latestSchedule == null) {
            throw ErrorCode.PERFORMANCE_NOT_FOUND.domainException("공연 스케줄이 존재하지 않습니다.");
        }
        if (latestSchedule.plusDays(7).isAfter(LocalDateTime.now())) {
            throw ErrorCode.INVALID_SETTLEMENT_REQUEST.domainException("공연 종료 후 7일이 지나야 정산 신청이 가능합니다.");
        }

        // 총 정산 금액 계산
        int totalAmount = calculateTotalAmount(schedules, performance);

        // Settlement 객체 생성
        // settledAt은 아직 값 설정하지 않고 나중에 confirm 할 때 설정
        Settlement settlement = Settlement.builder()
                .performanceId(request.performanceId())
                .totalAmount(totalAmount)
                .account(request.account())
                .bank(request.bank())
                .status(SettlementStatus.PENDING)
                .build();

        return settlementRepository.save(settlement).getId();
    }

    private int calculateTotalAmount(List<PerformanceSchedule> schedules, Performance performance) {
        int price = performance.getPrice();
        int totalSeats = performance.getTotalSeats();

        log.info("정산금액 계산 ======= 가격 {} 좌석수 {}", price, totalSeats);
        log.info("schedules = {}", schedules);

        // 스케쥴 리스트로 총 정산금액 누적 계산
        return schedules.stream()
                .filter(schedule -> !schedule.isCanceled()) // 취소된 스케쥴은 계산하지 않음
                .mapToInt(schedule -> price * (totalSeats - schedule.getRemainingSeats()))
                .sum();
    }

    /// PENDING 상태 정산의 은행, 계좌정보 수정
    @Transactional
    public SettlementUpdateResponse updateSettlement(SettlementUpdateRequest request) {
        log.info("[editSettlement Service] 요청: {}", request);
        Settlement settlement = settlementRepository.findById(request.settlementId())
                .orElseThrow(() -> ErrorCode.SETTLEMENT_NOT_FOUND.domainException("존재하지 않는 정산입니다."));

        // 승인된 정산은 정보를 수정할 수 없음
        if (settlement.getStatus() == SettlementStatus.CONFIRMED) {
            throw ErrorCode.INVALID_SETTLEMENT_REQUEST.domainException("이미 승인된 정산은 정보를 수정할 수 없습니다.");
        }

        Settlement updatedSettlement = settlement.updateBankInfo(request.bank(), request.account());
        return SettlementUpdateResponse.fromSettlement(updatedSettlement);
    }

    @Transactional
    public Long findSettlementIdByPerformanceId(Long performanceId) {
        return settlementRepository.findSettlementByPerformanceId(performanceId)
                .stream()
                .max((s1, s2) -> s1.getCreatedAt().compareTo(s2.getCreatedAt()))
                .map(Settlement::getId)
                .orElse(null);
    }

    @Transactional
    public SettlementResponse confirmSettlement(Long settlementId) {
        // 정산 객체 조회
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> ErrorCode.SETTLEMENT_NOT_FOUND.domainException("존재하지 않는 정산입니다."));

        // 공연 정보 조회
        Performance performance = performanceRepository.findById(settlement.getPerformanceId())
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("존재하지 않는 공연입니다."));

        // 정산 상태 변경 및 완료 시간 설정
        settlement.confirm();

        // SettlementResponse 생성 및 반환
        return SettlementResponse.fromEntity(settlement, performance.getTitle());
    }

    @Transactional(readOnly = true)
    public Page<SettlementResponse> findAllSettlementsWithUserId(Long userId, Pageable pageable) {
        return settlementRepository.findAllSettlementsWithUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<SettlementResponse> findAllSettlementsByStatus(String status, Pageable pageable) {
        if(status == null) {
            return settlementRepository.findAllSettlements(pageable);
        }
        
        SettlementStatus settlementStatus = getSettlementStatus(status);
        return settlementRepository.findAllSettlementsByStatus(settlementStatus, pageable);
    }

    // string -> settlementStatus로 변환. 변환 불가능할 경우 throw exception
    private static SettlementStatus getSettlementStatus(String settlementStatus) {
        SettlementStatus status;

        try { // 문자열 쿼리 파라미터를 대문자로 변환하여 settlementStatus 생성 시도
            status = SettlementStatus.valueOf(settlementStatus.toUpperCase());

        } catch (IllegalArgumentException e) {
            // 유효하지 않은 종류의 settlementStatus 문자열이 들어왔을 경우
            throw ErrorCode.INVALID_SETTLEMENT_STATUS.domainException("유효하지 않은 종류의 settlement status로 생성 요청하였습니다. status: "+ settlementStatus);
        }
        return status;
    }
}

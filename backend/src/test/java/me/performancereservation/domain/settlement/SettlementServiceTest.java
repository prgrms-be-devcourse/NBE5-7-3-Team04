//package me.performancereservation.domain.settlement;
//
//import me.performancereservation.domain.performance.entities.Performance;
//import me.performancereservation.domain.performance.entities.PerformanceSchedule;
//import me.performancereservation.domain.performance.repository.PerformanceRepository;
//import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
//import me.performancereservation.domain.settlement.dto.SettlementRequest;
//import me.performancereservation.domain.settlement.dto.SettlementResponse;
//import me.performancereservation.domain.settlement.enums.SettlementStatus;
//import me.performancereservation.global.exception.AppException;
//import me.performancereservation.global.exception.ErrorCode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@SpringBootTest
//@Transactional
//class SettlementServiceTest {
//
//    @Autowired
//    private SettlementService settlementService;
//
//    @Autowired
//    private SettlementRepository settlementRepository;
//
//    @Autowired
//    private PerformanceRepository performanceRepository;
//
//    @Autowired
//    private PerformanceScheduleRepository performanceScheduleRepository;
//
//    private Performance performance;
//    private PerformanceSchedule schedule1;
//    private PerformanceSchedule schedule2;
//    private Long managerId = 1L;
//
//    @BeforeEach
//    void setUp() {
//        // 공연 생성
//        performance = Performance.builder()
//                .title("테스트 공연")
//                .price(10000)
//                .totalSeats(100)
//                .managerId(managerId)
//                .build();
//        performanceRepository.save(performance);
//        SettlementServiceTestUtils.logPerformanceInfo(performance);
//
//        // 공연 스케줄 생성
//        schedule1 = PerformanceSchedule.builder()
//                .performanceId(performance.getId())
//                .startTime(LocalDateTime.now().minusDays(10))
//                .remainingSeats(80)
//                .canceled(false)
//                .build();
//
//        schedule2 = PerformanceSchedule.builder()
//                .performanceId(performance.getId())
//                .startTime(LocalDateTime.now().minusDays(8))
//                .remainingSeats(70)
//                .canceled(false)
//                .build();
//
//        List<PerformanceSchedule> schedules = performanceScheduleRepository.saveAll(List.of(schedule1, schedule2));
//        SettlementServiceTestUtils.logScheduleInfo(schedules);
//    }
//
//    @Test
//    @DisplayName("정산 생성 성공")
//    void createSettlementSuccess() {
//        // given
//        SettlementRequest request = new SettlementRequest(
//                performance.getId(),
//                "123-456-789",
//                "신한은행"
//        );
//
//        // when
//        Long settlementId = settlementService.createSettlement(request);
//
//        // then
//        Settlement settlement = settlementRepository.findById(settlementId).orElseThrow();
//        SettlementServiceTestUtils.logSettlementInfo(settlement);
//
//        assertThat(settlement.getPerformanceId()).isEqualTo(performance.getId());
//        assertThat(settlement.getAccount()).isEqualTo("123-456-789");
//        assertThat(settlement.getBank()).isEqualTo("신한은행");
//        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.PENDING);
//        assertThat(settlement.getTotalAmount()).isEqualTo(500000); // (100-80 + 100-70) * 10000
//    }
//
//    @Test
//    @DisplayName("공연 종료 후 7일이 지나지 않으면 정산 생성 실패")
//    void createSettlementFailBefore7Days() {
//        // given
//        PerformanceSchedule recentSchedule = PerformanceSchedule.builder()
//                .performanceId(performance.getId())
//                .startTime(LocalDateTime.now().minusDays(5))
//                .remainingSeats(90)
//                .canceled(false)
//                .build();
//        performanceScheduleRepository.save(recentSchedule);
//        SettlementServiceTestUtils.logScheduleInfo(List.of(recentSchedule));
//
//        SettlementRequest request = new SettlementRequest(
//                performance.getId(),
//                "123-456-789",
//                "신한은행"
//        );
//
//        // when & then
//        assertThatThrownBy(() -> settlementService.createSettlement(request))
//                .isInstanceOf(AppException.class) // 예외 타입 먼저 확인
//                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
//                        .isEqualTo(ErrorCode.INVALID_SETTLEMENT_REQUEST));
//    }
//
//    @Test
//    @DisplayName("정산 확인 성공")
//    void confirmSettlementSuccess() {
//        // given
//        Settlement settlement = Settlement.builder()
//                .performanceId(performance.getId())
//                .totalAmount(500000)
//                .account("123-456-789")
//                .bank("신한은행")
//                .status(SettlementStatus.PENDING)
//                .build();
//        settlementRepository.save(settlement);
//        SettlementServiceTestUtils.logSettlementInfo(settlement);
//
//        // when
//        SettlementResponse response = settlementService.confirmSettlement(settlement.getId());
//        SettlementServiceTestUtils.logSettlementResponse(response);
//
//        // then
//        assertThat(response.settlementId()).isEqualTo(settlement.getId());
//        assertThat(response.status()).isEqualTo(SettlementStatus.CONFIRMED);
//        assertThat(response.settledAt()).isNotNull();
//        assertThat(response.title()).isEqualTo("테스트 공연");
//    }
//
//    @Test
//    @DisplayName("존재하지 않는 정산 ID로 확인 시도 시 실패")
//    void confirmSettlementFailWithInvalidId() {
//        // when & then
//        assertThatThrownBy(() -> settlementService.confirmSettlement(999L))
//                .isInstanceOf(AppException.class) // 예외 타입 먼저 확인
//                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
//                        .isEqualTo(ErrorCode.SETTLEMENT_NOT_FOUND));
//    }
//
//    @Test
//    @DisplayName("사용자별 정산 목록 조회 성공")
//    void findAllSettlementsWithUserIdSuccess() {
//        // given
//        Settlement settlement1 = createSettlement(SettlementStatus.PENDING);
//        Settlement settlement2 = createSettlement(SettlementStatus.CONFIRMED);
//        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());
//
//        // when
//        Page<SettlementResponse> response = settlementService.findAllSettlementsWithUserId(managerId, pageRequest);
//
//        // then
//        assertThat(response.getContent()).hasSize(2);
//        assertThat(response.getContent().get(0).title()).isEqualTo("테스트 공연");
//        assertThat(response.getContent().get(0).status()).isIn(SettlementStatus.PENDING, SettlementStatus.CONFIRMED);
//
//        // 로깅 추가
//        response.getContent().forEach(SettlementServiceTestUtils::logSettlementResponse);
//    }
//
//    @Test
//    @DisplayName("전체 정산 목록 조회 성공")
//    void findAllSettlementsSuccess() {
//        // given
//        Settlement settlement1 = createSettlement(SettlementStatus.PENDING);
//        Settlement settlement2 = createSettlement(SettlementStatus.PENDING);
//        settlementService.confirmSettlement(settlement2.getId());
//        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());
//
//        // when
//        Page<SettlementResponse> response = settlementService.findAllSettlementsByStatus(null, pageRequest);
//
//        // then
//        assertThat(response.getContent()).hasSize(2);
//        assertThat(response.getContent().get(0).title()).isEqualTo("테스트 공연");
//        assertThat(response.getContent().get(0).status()).isIn(SettlementStatus.PENDING, SettlementStatus.CONFIRMED);
//
//        // 로깅 추가
//        response.getContent().forEach(SettlementServiceTestUtils::logSettlementResponse);
//    }
//
//    @Test
//    @DisplayName("상태별 정산 목록 조회 성공")
//    void findAllSettlementsByStatusSuccess() {
//        // given
//        Settlement settlement1 = createSettlement(SettlementStatus.PENDING);
//        Settlement settlement2 = createSettlement(SettlementStatus.CONFIRMED);
//        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());
//
//        // when
//        Page<SettlementResponse> response = settlementService.findAllSettlementsByStatus("PENDING", pageRequest);
//
//        // then
//        assertThat(response.getContent()).hasSize(1);
//        assertThat(response.getContent().get(0).status()).isEqualTo(SettlementStatus.PENDING);
//        assertThat(response.getContent().get(0).title()).isEqualTo("테스트 공연");
//
//        // 로깅 추가
//        response.getContent().forEach(SettlementServiceTestUtils::logSettlementResponse);
//    }
//
//    @Test
//    @DisplayName("잘못된 상태로 정산 목록 조회 시 실패")
//    void findAllSettlementsByStatusFailWithInvalidStatus() {
//        // given
//        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("createdAt").descending());
//
//        // when & then
//        assertThatThrownBy(() -> settlementService.findAllSettlementsByStatus("INVALID_STATUS", pageRequest))
//                .isInstanceOf(AppException.class)
//                .satisfies(e -> assertThat(((AppException) e).getErrorCode())
//                        .isEqualTo(ErrorCode.INVALID_SETTLEMENT_STATUS));
//    }
//
//    private Settlement createSettlement(SettlementStatus status) {
//        Settlement settlement = Settlement.builder()
//                .performanceId(performance.getId())
//                .totalAmount(500000)
//                .account("123-456-789")
//                .bank("신한은행")
//                .status(status)
//                .build();
//        return settlementRepository.save(settlement);
//    }
//}
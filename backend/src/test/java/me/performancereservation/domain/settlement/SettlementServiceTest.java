package me.performancereservation.domain.settlement;

import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.settlement.dto.SettlementRequest;
import me.performancereservation.domain.settlement.dto.SettlementResponse;
import me.performancereservation.domain.settlement.dto.SettlementUpdateRequest;
import me.performancereservation.domain.settlement.dto.SettlementUpdateResponse;
import me.performancereservation.domain.settlement.enums.SettlementStatus;
import me.performancereservation.global.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private PerformanceRepository performanceRepository;

    @Mock
    private PerformanceScheduleRepository performanceScheduleRepository;

    @InjectMocks
    private SettlementService settlementService;

    private static final Long PERFORMANCE_ID = 1L;
    private static final Long SCHEDULE_ID = 1L;
    private static final Long SETTLEMENT_ID = 1L;
    private static final Long FILE_ID = 1L;
    private static final Long MANAGER_ID = 1L;

    private Performance performance;
    private PerformanceSchedule schedule;
    private Settlement settlement;

    @BeforeEach
    void setUp() {
        performance = Performance.builder()
                .id(PERFORMANCE_ID)
                .title("오페라 갈라")
                .venue("세종문화회관 대극장")
                .price(120000)
                .totalSeats(2000)
                .category(PerformanceCategory.MUSICAL_OPERA)
                .startDate(LocalDateTime.of(2025, 12, 13, 0, 0))
                .endDate(LocalDateTime.of(2025, 12, 14, 0, 0))
                .description("한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!")
                .fileId(FILE_ID)
                .managerId(MANAGER_ID)
                .build();

        schedule = PerformanceSchedule.builder()
                .id(SCHEDULE_ID)
                .performanceId(PERFORMANCE_ID)
                .startTime(LocalDateTime.of(2025, 12, 13, 19, 0))
                .endTime(LocalDateTime.of(2025, 12, 13, 21, 0))
                .remainingSeats(1800)
                .canceled(false)
                .build();

        settlement = Settlement.builder()
                .id(SETTLEMENT_ID)
                .performanceId(PERFORMANCE_ID)
                .totalAmount(24000000)
                .account("123-456-789")
                .bank("신한은행")
                .status(SettlementStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("정산 생성 성공 테스트")
    void createSettlement_Success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenDaysAgo = now.minusDays(10);

        Performance performance = Performance.builder()
                .id(PERFORMANCE_ID)
                .title("오페라 갈라")
                .venue("세종문화회관 대극장")
                .price(120000)
                .totalSeats(2000)
                .category(PerformanceCategory.MUSICAL_OPERA)
                .startDate(tenDaysAgo.minusDays(1))
                .endDate(tenDaysAgo)
                .description("한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!")
                .fileId(FILE_ID)
                .managerId(MANAGER_ID)
                .build();

        PerformanceSchedule schedule = PerformanceSchedule.builder()
                .id(SCHEDULE_ID)
                .performanceId(PERFORMANCE_ID)
                .startTime(tenDaysAgo.minusHours(2))
                .endTime(tenDaysAgo)
                .remainingSeats(1800)
                .canceled(false)
                .build();

        SettlementRequest request = new SettlementRequest(
                PERFORMANCE_ID,
                "123-456-789",
                "신한은행"
        );
        when(performanceRepository.findById(PERFORMANCE_ID)).thenReturn(Optional.of(performance));
        when(performanceScheduleRepository.findByPerformanceIdOrderByStartTimeAsc(PERFORMANCE_ID))
                .thenReturn(List.of(schedule));
        when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);

        // when
        Long settlementId = settlementService.createSettlement(request);

        // then
        assertThat(settlementId).isEqualTo(SETTLEMENT_ID);
        verify(performanceRepository).findById(PERFORMANCE_ID);
        verify(performanceScheduleRepository).findByPerformanceIdOrderByStartTimeAsc(PERFORMANCE_ID);
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    @DisplayName("정산 생성 실패 테스트 - 존재하지 않는 공연")
    void createSettlement_Fail_PerformanceNotFound() {
        // given
        SettlementRequest request = new SettlementRequest(
                PERFORMANCE_ID,
                "123-456-789",
                "신한은행"
        );
        when(performanceRepository.findById(PERFORMANCE_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> settlementService.createSettlement(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("존재하지 않는 공연입니다");
    }

    @Test
    @DisplayName("정산 정보 수정 성공 테스트")
    void updateSettlement_Success() {
        // given
        SettlementUpdateRequest request = new SettlementUpdateRequest(
                SETTLEMENT_ID,
                "국민은행",
                "987-654-321"
        );
        when(settlementRepository.findById(SETTLEMENT_ID)).thenReturn(Optional.of(settlement));

        // when
        SettlementUpdateResponse response = settlementService.updateSettlement(request);

        // then
        assertThat(response.settlementId()).isEqualTo(SETTLEMENT_ID);
        assertThat(response.bank()).isEqualTo("국민은행");
        assertThat(response.account()).isEqualTo("987-654-321");
        verify(settlementRepository).findById(SETTLEMENT_ID);
    }

    @Test
    @DisplayName("정산 정보 수정 실패 테스트 - 존재하지 않는 정산")
    void updateSettlement_Fail_SettlementNotFound() {
        // given
        SettlementUpdateRequest request = new SettlementUpdateRequest(
                SETTLEMENT_ID,
                "국민은행",
                "987-654-321"
        );
        when(settlementRepository.findById(SETTLEMENT_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> settlementService.updateSettlement(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("존재하지 않는 정산입니다");
    }

    @Test
    @DisplayName("정산 승인 성공 테스트")
    void confirmSettlement_Success() {
        // given
        when(settlementRepository.findById(SETTLEMENT_ID)).thenReturn(Optional.of(settlement));
        when(performanceRepository.findById(PERFORMANCE_ID)).thenReturn(Optional.of(performance));

        // when
        SettlementResponse response = settlementService.confirmSettlement(SETTLEMENT_ID);

        // then
        assertThat(response.settlementId()).isEqualTo(SETTLEMENT_ID);
        assertThat(response.status()).isEqualTo(SettlementStatus.CONFIRMED);
        assertThat(response.settledAt()).isNotNull();
        verify(settlementRepository).findById(SETTLEMENT_ID);
        verify(performanceRepository).findById(PERFORMANCE_ID);
    }

    @Test
    @DisplayName("정산 승인 실패 테스트 - 존재하지 않는 정산")
    void confirmSettlement_Fail_SettlementNotFound() {
        // given
        when(settlementRepository.findById(SETTLEMENT_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> settlementService.confirmSettlement(SETTLEMENT_ID))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("존재하지 않는 정산입니다");
    }

    @Test
    @DisplayName("사용자별 정산 목록 조회 성공 테스트")
    void findAllSettlementsWithUserId_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        SettlementResponse response = new SettlementResponse(
                SETTLEMENT_ID,
                24000000,
                LocalDateTime.now(),
                "123-456-789",
                "신한은행",
                SettlementStatus.PENDING,
                "오페라 갈라"
        );
        Page<SettlementResponse> page = new PageImpl<>(List.of(response));
        when(settlementRepository.findAllSettlementsWithUserId(MANAGER_ID, pageable)).thenReturn(page);

        // when
        Page<SettlementResponse> result = settlementService.findAllSettlementsWithUserId(MANAGER_ID, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).settlementId()).isEqualTo(SETTLEMENT_ID);
        verify(settlementRepository).findAllSettlementsWithUserId(MANAGER_ID, pageable);
    }

    @Test
    @DisplayName("상태별 정산 목록 조회 성공 테스트")
    void findAllSettlementsByStatus_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        SettlementResponse response = new SettlementResponse(
                SETTLEMENT_ID,
                24000000,
                LocalDateTime.now(),
                "123-456-789",
                "신한은행",
                SettlementStatus.PENDING,
                "오페라 갈라"
        );
        Page<SettlementResponse> page = new PageImpl<>(List.of(response));
        when(settlementRepository.findAllSettlementsByStatus(SettlementStatus.PENDING, pageable)).thenReturn(page);

        // when
        Page<SettlementResponse> result = settlementService.findAllSettlementsByStatus("PENDING", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).settlementId()).isEqualTo(SETTLEMENT_ID);
        verify(settlementRepository).findAllSettlementsByStatus(SettlementStatus.PENDING, pageable);
    }
}

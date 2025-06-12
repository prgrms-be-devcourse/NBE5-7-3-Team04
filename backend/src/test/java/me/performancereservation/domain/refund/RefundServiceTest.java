package me.performancereservation.domain.refund;

import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.domain.refund.dto.RefundResponse;
import me.performancereservation.domain.refund.dto.UpdateBankInfoRequest;
import me.performancereservation.domain.refund.enums.RefundStatus;
import me.performancereservation.domain.refund.mapper.RefundDetailMapper;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.enums.ReservationStatus;
import me.performancereservation.domain.sms.SMSService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefundServiceTest {

    @Mock
    private RefundRepository refundRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RefundDetailMapper refundDetailMapper;

    @Mock
    private SMSService smsService;

    @InjectMocks
    private RefundService refundService;

    private static final Long PERFORMANCE_ID = 1L;
    private static final Long SCHEDULE_ID = 1L;
    private static final Long RESERVATION_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long REFUND_ID = 1L;
    private static final Long FILE_ID = 1L;
    private static final Long MANAGER_ID = 1L;

    private Performance performance;
    private Reservation reservation;
    private Refund refund;

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

        reservation = Reservation.builder()
                .id(RESERVATION_ID)
                .scheduleId(SCHEDULE_ID)
                .userId(USER_ID)
                .quantity(2)
                .status(ReservationStatus.PAYMENTS_CONFIRMED)
                .build();

        refund = Refund.builder()
                .id(REFUND_ID)
                .reservationId(RESERVATION_ID)
                .userId(USER_ID)
                .status(RefundStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("환불 생성 성공 테스트")
    void save_Success() {
        // given
        when(refundRepository.findRefundByReservationId(RESERVATION_ID)).thenReturn(Optional.empty());
        when(refundRepository.save(any(Refund.class))).thenReturn(refund);

        // when
        refundService.save(reservation);

        // then
        verify(refundRepository).findRefundByReservationId(RESERVATION_ID);
        verify(refundRepository).save(any(Refund.class));
    }

    @Test
    @DisplayName("환불 생성 실패 테스트 - 이미 환불 신청된 예약")
    void save_Fail_DuplicateRefund() {
        // given
        when(refundRepository.findRefundByReservationId(RESERVATION_ID)).thenReturn(Optional.of(refund));

        // when & then
        assertThatThrownBy(() -> refundService.save(reservation))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("이미 존재하는 환불내역입니다.");
    }

    @Test
    @DisplayName("환불 상태 업데이트 성공 테스트")
    void updateRefundStatus_Success() {
        // given
        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));

        // when
        refundService.updateRefundStatus(REFUND_ID, "READY");

        // then
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.READY);
    }

    @Test
    @DisplayName("환불 상태 업데이트 실패 테스트 - 존재하지 않는 환불")
    void updateRefundStatus_Fail_RefundNotFound() {
        // given
        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> refundService.updateRefundStatus(REFUND_ID, "READY"))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("존재하지 않는 환불ID입니다");
    }

    @Test
    @DisplayName("환불 승인 성공 테스트")
    void confirmRefund_Success() {
        // given
        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));
        when(reservationRepository.findById(RESERVATION_ID)).thenReturn(Optional.of(reservation));

        // when
        refundService.confirmRefund(REFUND_ID);

        // then
        assertThat(refund.getStatus()).isEqualTo(RefundStatus.CONFIRMED);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCEL_CONFIRMED);
        verify(smsService, never()).refundConfirmed(any());
    }

    @Test
    @DisplayName("계좌 정보 업데이트 성공 테스트")
    void updateBankInfo_Success() {
        // given
        UpdateBankInfoRequest request = new UpdateBankInfoRequest(
                REFUND_ID,
                "123-456-789",
                "신한은행",
                "홍길동"
        );
        when(refundRepository.findById(REFUND_ID)).thenReturn(Optional.of(refund));

        // when
        Refund updatedRefund = refundService.updateBankInfo(USER_ID, request);

        // then
        assertThat(updatedRefund.getAccount()).isEqualTo("123-456-789");
        assertThat(updatedRefund.getBank()).isEqualTo("신한은행");
        assertThat(updatedRefund.getDepositorName()).isEqualTo("홍길동");
        assertThat(updatedRefund.getStatus()).isEqualTo(RefundStatus.READY);
    }

    @Test
    @DisplayName("사용자별 환불 내역 조회 성공 테스트")
    void findAllRefundsDetailByUserId_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> results = new ArrayList<>();
        Object[] result = new Object[]{refund, 2, LocalDateTime.now(), performance};
        results.add(result);

        Page<Object[]> page = new PageImpl<>(results);
        when(refundRepository.findRefundsDetailByUserId(USER_ID, pageable)).thenReturn(page);

        RefundDetailResponse expectedResponse = new RefundDetailResponse(
                REFUND_ID,
                USER_ID,
                RESERVATION_ID,
                null,
                null,
                null,
                RefundStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now(),
                2,
                LocalDateTime.now(),
                FILE_ID,
                "오페라 갈라",
                "세종문화회관 대극장",
                120000,
                "MUSICAL_OPERA",
                LocalDateTime.now(),
                "한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!"
        );
        Page<RefundDetailResponse> expectedPage = new PageImpl<>(List.of(expectedResponse));
        when(refundDetailMapper.toRefundDetailResponsePage(page)).thenReturn(expectedPage);

        // when
        Page<RefundDetailResponse> response = refundService.findAllRefundsDetailByUserId(USER_ID, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        verify(refundRepository).findRefundsDetailByUserId(USER_ID, pageable);
        verify(refundDetailMapper).toRefundDetailResponsePage(page);
    }

    @Test
    @DisplayName("환불 상세 조회 성공 테스트")
    void findRefundsDetailByRefundId_Success() {
        // given
        List<Object[]> results = new ArrayList<>();
        Object[] result = new Object[]{refund, 2, LocalDateTime.now(), performance};
        results.add(result);

        when(refundRepository.findRefundsDetailByRefundId(REFUND_ID)).thenReturn(results);
        when(refundDetailMapper.toRefundDetailResponse(any())).thenReturn(
                new RefundDetailResponse(
                        REFUND_ID,
                        USER_ID,
                        RESERVATION_ID,
                        null,
                        null,
                        null,
                        RefundStatus.PENDING,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        2,
                        LocalDateTime.now(),
                        FILE_ID,
                        "오페라 갈라",
                        "세종문화회관 대극장",
                        120000,
                        "MUSICAL_OPERA",
                        LocalDateTime.now(),
                        "한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!"
                )
        );

        // when
        RefundDetailResponse response = refundService.findRefundsDetailByRefundId(REFUND_ID);

        // then
        assertThat(response).isNotNull();
        verify(refundRepository).findRefundsDetailByRefundId(REFUND_ID);
    }

    @Test
    @DisplayName("환불 상세 조회 실패 테스트 - 존재하지 않는 환불")
    void findRefundsDetailByRefundId_Fail_RefundNotFound() {
        // given
        when(refundRepository.findRefundsDetailByRefundId(REFUND_ID)).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> refundService.findRefundsDetailByRefundId(REFUND_ID))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("존재하지 않는 환불ID입니다");
    }
}

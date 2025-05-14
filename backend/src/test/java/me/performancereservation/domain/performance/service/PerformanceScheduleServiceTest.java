package me.performancereservation.domain.performance.service;

import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleRequest;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.global.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceScheduleServiceTest {

    @Mock
    PerformanceRepository performanceRepository;

    @Mock
    PerformanceScheduleRepository performanceScheduleRepository;

    @InjectMocks
    PerformanceScheduleService scheduleService;

    static final Long PERFORMANCE_ID = 1L;
    static final Long PERFORMANCE_SCHEDULE_ID = 1L;

    static final Long PERFORMANCE_ID_NOT_CONFIRM = 2L;
    static final Long FILE_ID = 1L;

    static final Long MANAGER_ID = 1L;

    Performance performance;
    PerformanceSchedule schedule;

    Performance performanceNotConfirm;

    @BeforeEach
    void init() {
        performance = Performance.builder()
                .id(PERFORMANCE_ID)
                .title("오페라 갈라")
                .venue("세종문화회관 대극장")
                .price(120000)
                .totalSeats(2000)
                .category(PerformanceCategory.OPERA)
                .performanceDate(LocalDateTime.of(2025, 12, 13, 0, 0))
                .description("한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!")
                .fileId(FILE_ID)
                .managerId(MANAGER_ID)
                .status(PerformanceStatus.CONFIRMED)
                .build();

        performanceNotConfirm = Performance.builder()
                .id(PERFORMANCE_ID_NOT_CONFIRM)
                .title("오페라 갈라")
                .venue("세종문화회관 대극장")
                .price(120000)
                .totalSeats(2000)
                .category(PerformanceCategory.OPERA)
                .performanceDate(LocalDateTime.of(2025, 12, 13, 0, 0))
                .description("한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!")
                .fileId(FILE_ID)
                .managerId(MANAGER_ID)
                .status(PerformanceStatus.PENDING)
                .build();

        schedule = PerformanceSchedule.builder()
                .id(PERFORMANCE_SCHEDULE_ID)
                .performanceId(PERFORMANCE_ID)
                .startTime(LocalDateTime.of(2025, 12, 13, 9, 0))
                .endTime(LocalDateTime.of(2025, 12, 13, 10, 0))
                .canceled(false)
                .build();
    }


    @Test
    @DisplayName("회차 등록 성공 테스트")
    void createPerformanceSchedule_Success() {
        //given
        PerformanceScheduleRequest request = new PerformanceScheduleRequest(
                LocalDateTime.of(2025, 12, 13, 9, 0),
                LocalDateTime.of(2025, 12, 13, 10, 0)
        );
        when(performanceRepository.findById(PERFORMANCE_ID)).thenReturn(Optional.of(performance));
        when(performanceScheduleRepository.save(any(PerformanceSchedule.class))).thenReturn(schedule);

        //when
        Long savedScheduleId = scheduleService.createPerformanceSchedule(PERFORMANCE_ID, request, MANAGER_ID);

        //then
        assertThat(savedScheduleId).isEqualTo(PERFORMANCE_SCHEDULE_ID);
        verify(performanceScheduleRepository).save(any(PerformanceSchedule.class));
    }

    @Test
    @DisplayName("회차 등록 실패 테스트 - 공연 등록 대기 상태")
    void createPerformanceSchedule_ThrowsWhenPerformanceNotConfirmed() {
        //given
        PerformanceScheduleRequest request = new PerformanceScheduleRequest(
                LocalDateTime.of(2025, 12, 13, 9, 0),
                LocalDateTime.of(2025, 12, 13, 10, 0)
        );
        when(performanceRepository.findById(PERFORMANCE_ID_NOT_CONFIRM)).thenReturn(Optional.of(performanceNotConfirm));

        //when & then
        assertThatThrownBy(() -> scheduleService.createPerformanceSchedule(PERFORMANCE_ID_NOT_CONFIRM, request, MANAGER_ID))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("공연이 승인 대기 중입니다. 승인 후 회차 등록이 가능합니다.");
    }


    @Test
    @DisplayName("회차 취소 성공 테스트")
    void cancelPerformanceSchedule_Success() {
        //given
        when(performanceScheduleRepository.findById(PERFORMANCE_SCHEDULE_ID)).thenReturn(Optional.of(schedule));
        when(performanceRepository.findById(PERFORMANCE_ID)).thenReturn(Optional.of(performance));

        //when
        Long canceledId = scheduleService.cancelPerformanceSchedule(PERFORMANCE_ID, PERFORMANCE_SCHEDULE_ID, MANAGER_ID);

        //then
        assertThat(schedule.isCanceled()).isTrue();
        assertThat(canceledId).isEqualTo(PERFORMANCE_SCHEDULE_ID);
    }

}
package me.performancereservation.domain.performance.service;

import me.performancereservation.domain.file.File;
import me.performancereservation.domain.file.FileRepository;
import me.performancereservation.domain.performance.dto.performance.request.PerformanceCreateRequest;
import me.performancereservation.domain.performance.dto.performance.request.PerformanceUpdateRequest;
import me.performancereservation.domain.performance.dto.performance.response.PerformanceDetailResponse;
import me.performancereservation.domain.performance.dto.performance.response.PerformancePageResponse;
import me.performancereservation.domain.performance.dto.performance.response.PerformanceManagerPageResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

    @Mock
    PerformanceRepository performanceRepository;

    @Mock
    PerformanceScheduleRepository performanceScheduleRepository;

    @Mock
    FileRepository fileRepository;

    @InjectMocks
    PerformanceService performanceService;

    static final Long PERFORMANCE_ID1 = 1L;
    static final Long PERFORMANCE_ID2 = 2L;
    static final Long PERFORMANCE_ID3 = 3L;
    static final Long PERFORMANCE_SCHEDULE_ID1 = 1L;
    static final Long PERFORMANCE_SCHEDULE_ID2 = 2L;
    static final Long PERFORMANCE_SCHEDULE_ID3 = 3L;
    static final Long PERFORMANCE_SCHEDULE_ID4 = 4L;
    static final Long PERFORMANCE_SCHEDULE_ID5 = 5L;
    static final Long PERFORMANCE_SCHEDULE_ID6 = 6L;
    static final Long PERFORMANCE_SCHEDULE_ID7 = 7L;
    static final Long FILE_ID1 = 1L;
    static final Long FILE_ID2 = 2L;
    static final Long FILE_ID3 = 3L;
    static final Long MANAGER_ID = 1L;

    Performance performance1;
    Performance performance2;
    Performance performance3;
    PerformanceSchedule schedule1;
    PerformanceSchedule schedule2;
    PerformanceSchedule schedule3;
    PerformanceSchedule schedule4;
    PerformanceSchedule schedule5;
    PerformanceSchedule schedule6;
    PerformanceSchedule schedule7;
    File file1;
    File file2;
    File file3;

    @BeforeEach
    void init() {
        performance1 = Performance.builder()
                .id(PERFORMANCE_ID1)
                .title("오페라 갈라")
                .venue("세종문화회관 대극장")
                .price(120000)
                .totalSeats(2000)
                .category(PerformanceCategory.OPERA)
                .performanceDate(LocalDateTime.of(2025, 12, 13, 0, 0))
                .description("한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!")
                .fileId(FILE_ID1)
                .managerId(MANAGER_ID)
                .status(PerformanceStatus.CONFIRMED)
                .build();

        performance2 = Performance.builder()
                .id(PERFORMANCE_ID2)
                .title("부산항축제")
                .venue("부산항국제여객터미널 야외주차장")
                .price(5000)
                .totalSeats(1000)
                .category(PerformanceCategory.SINGING)
                .performanceDate(LocalDateTime.of(2025, 5, 30, 0, 0))
                .description("--")
                .fileId(FILE_ID2)
                .managerId(MANAGER_ID)
                .status(PerformanceStatus.CONFIRMED)
                .build();

        performance3 = Performance.builder()
                .id(PERFORMANCE_ID3)
                .title("우리가 만든 음악섬")
                .venue("LG아트센터 서울 U+스테이지")
                .price(77000)
                .totalSeats(500)
                .category(PerformanceCategory.SINGING)
                .performanceDate(LocalDateTime.of(2025, 6, 1, 0, 0))
                .description("여름의 초입, LG아트센터 서울, U+ 스테이지에 피어나는 음악섬")
                .fileId(FILE_ID3)
                .managerId(MANAGER_ID)
                .status(PerformanceStatus.CONFIRMED)
                .build();

        schedule1 = PerformanceSchedule.builder()
                .id(PERFORMANCE_SCHEDULE_ID1)
                .performanceId(PERFORMANCE_ID1)
                .startTime(LocalDateTime.of(2025, 12, 13, 9, 0))
                .endTime(LocalDateTime.of(2025, 12, 13, 10, 0))
                .canceled(false)
                .build();

        schedule2 = PerformanceSchedule.builder()
                .id(PERFORMANCE_SCHEDULE_ID2)
                .performanceId(PERFORMANCE_ID1)
                .startTime(LocalDateTime.of(2025, 12, 13, 11, 0))
                .endTime(LocalDateTime.of(2025, 12, 13, 12, 0))
                .canceled(false)
                .build();

        schedule3 = PerformanceSchedule.builder()
                .id(PERFORMANCE_SCHEDULE_ID3)
                .performanceId(PERFORMANCE_ID1)
                .startTime(LocalDateTime.of(2025, 12, 13, 13, 0))
                .endTime(LocalDateTime.of(2025, 12, 13, 14, 0))
                .canceled(false)
                .build();

        schedule4 = PerformanceSchedule.builder()
                .id(PERFORMANCE_SCHEDULE_ID4)
                .performanceId(PERFORMANCE_ID2)
                .startTime(LocalDateTime.of(2025, 5, 30, 12, 0))
                .endTime(LocalDateTime.of(2025, 5, 30, 14, 0))
                .canceled(false)
                .build();

        schedule5 = PerformanceSchedule.builder()
                .id(PERFORMANCE_SCHEDULE_ID5)
                .performanceId(PERFORMANCE_ID2)
                .startTime(LocalDateTime.of(2025, 5, 30, 15, 0))
                .endTime(LocalDateTime.of(2025, 5, 30, 17, 0))
                .canceled(false)
                .build();

        schedule6 = PerformanceSchedule.builder()
                .id(PERFORMANCE_SCHEDULE_ID6)
                .performanceId(PERFORMANCE_ID2)
                .startTime(LocalDateTime.of(2025, 5, 30, 18, 0))
                .endTime(LocalDateTime.of(2025, 5, 30, 20, 0))
                .canceled(false)
                .build();

        schedule7 = PerformanceSchedule.builder()
                .id(PERFORMANCE_SCHEDULE_ID7)
                .performanceId(PERFORMANCE_ID3)
                .startTime(LocalDateTime.of(2025, 6, 1, 18, 0))
                .endTime(LocalDateTime.of(2025, 6, 1, 21, 0))
                .remainingSeats(0)
                .canceled(false)
                .build();

        file1 = File.builder()
                .id(FILE_ID1)
                .key("파일경로1")
                .build();

        file2 = File.builder()
                .id(FILE_ID2)
                .key("파일경로2")
                .build();

        file3 = File.builder()
                .id(FILE_ID3)
                .key("파일경로3")
                .build();
    }

    @Test
    @DisplayName("공연 생성 성공 테스트")
    void createPerformance_Success() {
        //given
        PerformanceCreateRequest request = new PerformanceCreateRequest(
                "오페라 갈라",
                "세종문화회관 대극장",
                120000,
                2000,
                "OPERA",
                LocalDateTime.of(2025,12,13, 0,0),
                "한자리에서 만나는 오페라 명곡들 그리고 오페라 스타들!",
                FILE_ID1
        );
        when(performanceRepository.save(any(Performance.class))).thenReturn(performance1);

        //when
        Long savedId = performanceService.createPerformance(request, MANAGER_ID);

        //then
        assertThat(savedId).isNotNull();
        assertThat(savedId).isEqualTo(PERFORMANCE_ID1);
        verify(performanceRepository).save(any(Performance.class));
    }

    @Test
    @DisplayName("공연 수정 성공 테스트")
    void updatePerformance_Success() {
        //given
        File file = File.builder()
                .id(FILE_ID1)
                .key("파일url")
                .build();

        PerformanceUpdateRequest request = new PerformanceUpdateRequest(
                file.getId(),
                "변경된 설명"
        );

        when(performanceRepository.findById(PERFORMANCE_ID1)).thenReturn(Optional.of(performance1));

        //when
        Long updatedId = performanceService.updatePerformance(performance1.getId(), request, MANAGER_ID);

        //then
        assertThat(updatedId).isEqualTo(PERFORMANCE_ID1);
        assertThat(performance1.getFileId()).isEqualTo(request.fileId());
        assertThat(performance1.getDescription()).isEqualTo(request.description());
    }

    @Test
    @DisplayName("공연 수정 실패 - 공연 없음")
    void updatePerformance_Fail() {
        //given
        PerformanceUpdateRequest request = new PerformanceUpdateRequest(
                FILE_ID1,
                "수정된 설명"
        );
        when(performanceRepository.findById(PERFORMANCE_ID1)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> performanceService.updatePerformance(PERFORMANCE_ID1, request, MANAGER_ID))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("해당 공연을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("공연 수정 실패 - 권한 없음")
    void updatePerformance_FailNoPermission() {
        //given
        PerformanceUpdateRequest request = new PerformanceUpdateRequest(
                FILE_ID1,
                "수정된 설명"
        );
        when(performanceRepository.findById(PERFORMANCE_ID1)).thenReturn(Optional.of(performance1));

        //when & then
        assertThatThrownBy(() -> performanceService.updatePerformance(PERFORMANCE_ID1, request, 10L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("공연 취소 성공 테스트")
    void cancelPerformance_Success() {
        //given
        when(performanceRepository.findById(PERFORMANCE_ID1)).thenReturn(Optional.of(performance1));

        when(performanceScheduleRepository.findByPerformanceId(PERFORMANCE_ID1))
                .thenReturn(List.of(schedule1, schedule2, schedule3));

        //when
        performanceService.cancelPerformance(PERFORMANCE_ID1, MANAGER_ID);

        //then
        assertThat(performance1.getStatus()).isEqualTo(PerformanceStatus.CANCELLED);
        assertThat(schedule1.isCanceled()).isTrue();
        assertThat(schedule2.isCanceled()).isTrue();
        assertThat(schedule3.isCanceled()).isTrue();
        verify(performanceScheduleRepository).findByPerformanceId(PERFORMANCE_ID1);
    }

    @Test
    @DisplayName("공연 취소 실패 - 공연 없음")
    void cancelPerformance_Fail() {
        //given
        when(performanceRepository.findById(PERFORMANCE_ID1)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> performanceService.cancelPerformance(PERFORMANCE_ID1, MANAGER_ID))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("해당 공연을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("공연 취소 실패 - 권한 없음")
    void cancelPerformance_FailNoPermission() {
        //given
        when(performanceRepository.findById(PERFORMANCE_ID1)).thenReturn(Optional.of(performance1));

        //when & then
        assertThatThrownBy(() -> performanceService.cancelPerformance(PERFORMANCE_ID1, 10L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("접근 권한이 없습니다.");
    }

    @Test
    @DisplayName("공연 상세 조회 성공 테스트")
    void getPerformanceDetail_Success() {
        //given
        when(performanceRepository.findById(PERFORMANCE_ID1)).thenReturn(Optional.of(performance1));
        when(fileRepository.findById(FILE_ID1)).thenReturn(Optional.of((file1)));
        when(performanceScheduleRepository.findByPerformanceId(PERFORMANCE_ID1)).thenReturn(List.of(schedule1, schedule2, schedule3));

        //when
        PerformanceDetailResponse response = performanceService.getPerformanceDetail(PERFORMANCE_ID1);

        //then
        assertThat(response.id()).isEqualTo(PERFORMANCE_ID1);
        assertThat(response.schedules()).hasSize(3);
        assertThat(response.title()).isEqualTo(performance1.getTitle());
    }

    @Test
    @DisplayName("공연 상세 조회 실패 테스트 - 공연 없음")
    void getPerformanceDetail_Failure() {
        //given
        when(performanceRepository.findById(PERFORMANCE_ID1)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> performanceService.getPerformanceDetail(PERFORMANCE_ID1))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("해당 공연을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("공연자 목록 조회 성공 테스트")
    void getPerformanceManagerList_Success() {
        //given
        Page<Performance> page = new PageImpl<>(List.of(performance1, performance2, performance3));
        when(performanceRepository.findByManagerId(MANAGER_ID, Pageable.unpaged())).thenReturn(page);
        when(fileRepository.findAllById(List.of(FILE_ID1, FILE_ID2, FILE_ID3))).thenReturn(List.of(file1, file2, file3));

        //when
        Page<PerformanceManagerPageResponse> response = performanceService.getPerformanceManagerList(Pageable.unpaged(), MANAGER_ID);

        //then
        assertThat(response.getContent()).hasSize(3);
        assertThat(response.getContent().get(0).fileUrl()).isEqualTo("파일경로1");
        assertThat(response.getContent().get(0).status()).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("유저 공연 목록 조회 성공 테스트")
    void getPerformanceList_Success() {
        //given
        Page<Performance> page = new PageImpl<>(List.of(performance1, performance2));
        when(performanceRepository.findAvailablePerformances(Pageable.unpaged())).thenReturn(page);

        //when
        Page<PerformancePageResponse> response = performanceService.getPerformanceList(Pageable.unpaged());

        //then
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent())
                .extracting(PerformancePageResponse::id)
                .doesNotContain(PERFORMANCE_ID3);
    }

}
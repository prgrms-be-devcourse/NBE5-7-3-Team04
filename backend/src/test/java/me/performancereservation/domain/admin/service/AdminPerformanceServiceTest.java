package me.performancereservation.domain.admin.service;

import me.performancereservation.domain.admin.dto.response.PendingManagerRequestPageResponse;
import me.performancereservation.domain.admin.dto.response.PendingPerformancePageResponse;
import me.performancereservation.domain.admin.dto.response.PendingPerformanceScheduleResponse;
import me.performancereservation.domain.admin.mapper.AdminManagerRequestMapper;
import me.performancereservation.domain.admin.mapper.AdminPerformanceMapper;
import me.performancereservation.domain.admin.repository.AdminManagerRequestRepository;
import me.performancereservation.domain.admin.repository.AdminPerformanceRepository;
import me.performancereservation.domain.admin.repository.AdminPerformanceScheduleRepository;
import me.performancereservation.domain.admin.repository.AdminUserRepository;
import me.performancereservation.domain.file.File;
import me.performancereservation.domain.file.FileRepository;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.user.entitiy.ManagerRequest;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.ManagerRequestStatus;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.repository.UserRepository;
import me.performancereservation.global.exception.AppException;
import me.performancereservation.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminPerformanceServiceTest {

    @Mock
    private AdminPerformanceRepository adminPerformanceRepository;

    @Mock
    private AdminPerformanceScheduleRepository adminPerformanceScheduleRepository;

    @Mock
    private AdminManagerRequestRepository adminManagerRequestRepository;

    @Mock
    private AdminUserRepository adminUserRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminPerformanceService adminPerformanceService;

//    @Test
//    @DisplayName("PENDING 상태의 공연 목록 조회 성공")
//    void getPendingPerformanceList_Success() {
//        // given
//        Pageable pageable = PageRequest.of(0, 10);
//
//        // 공연 엔티티 생성
//        Performance performance = Performance.builder()
//                .id(1L)
//                .fileId(1L)
//                .managerId(1L)
//                .title("테스트 공연")
//                .venue("테스트 장소")
//                .price(10000)
//                .totalSeats(100)
//                .category(PerformanceCategory.OPERA)
//                .startDate(LocalDateTime.now().plusDays(7))
//                .endDate(LocalDateTime.now().plusDays(8))
//                .description("테스트 설명")
//                .status(PerformanceStatus.PENDING)
//                .build();
//
//        // 공연 스케줄 생성
//        PerformanceSchedule schedule = PerformanceSchedule.builder()
//                .id(1L)
//                .performanceId(1L)
//                .startTime(LocalDateTime.now().plusDays(7))
//                .endTime(LocalDateTime.now().plusDays(7).plusHours(2))
//                .remainingSeats(100)
//                .canceled(false)
//                .build();
//
//        // 파일 생성
//        File file = File.builder()
//                .id(1L)
//                .key("test-file-url")
//                .build();
//
//        // 사용자 생성
//        User user = User.builder()
//                .id(1L)
//                .email("test@test.com")
//                .name("테스트 매니저")
//                .phoneNumber("010-1234-5678")
//                .role(Role.MANAGER)
//                .build();
//
//        // 응답 DTO 생성
//        PendingPerformanceScheduleResponse scheduleResponse = new PendingPerformanceScheduleResponse(
//                1L,
//                LocalDateTime.now().plusDays(7),
//                LocalDateTime.now().plusDays(7).plusHours(2)
//        );
//
//        PendingPerformancePageResponse expectedResponse = new PendingPerformancePageResponse(
//                1L,
//                "test-file-url",
//                "테스트 매니저",
//                "테스트 공연",
//                "테스트 장소",
//                10000,
//                100,
//                PerformanceCategory.OPERA,
//                LocalDateTime.now().plusDays(7),
//                LocalDateTime.now().plusDays(8),
//                "테스트 설명",
//                List.of(scheduleResponse)
//        );
//
//        // when
//        when(adminPerformanceRepository.findAllByStatusOrderByCreatedAt(eq(PerformanceStatus.PENDING), eq(pageable)))
//                .thenReturn(new PageImpl<>(List.of(performance)));
//        when(fileRepository.findAllById(anyList()))
//                .thenReturn(List.of(file));
//        when(userRepository.findAllById(anyList()))
//                .thenReturn(List.of(user));
//        when(adminPerformanceScheduleRepository.findByPerformanceIdIn(anyList()))
//                .thenReturn(List.of(schedule));
//
//        // static 메서드 모킹
//        try (MockedStatic<AdminPerformanceMapper> mockedMapper = Mockito.mockStatic(AdminPerformanceMapper.class)) {
//            mockedMapper.when(() -> AdminPerformanceMapper.toScheduleResponse(any(PerformanceSchedule.class)))
//                    .thenReturn(scheduleResponse);
//            mockedMapper.when(() -> AdminPerformanceMapper.toPendingResponse(
//                            any(Performance.class),
//                            anyString(),
//                            anyString(),
//                            anyList()))
//                    .thenReturn(expectedResponse);
//
//            Page<PendingPerformancePageResponse> result = adminPerformanceService.getPendingPerformanceList(pageable, PerformanceStatus.PENDING);
//
//            // then
//            assertNotNull(result);
//            assertEquals(1, result.getTotalElements());
//            assertEquals(expectedResponse, result.getContent().get(0));
//        }
//
//        verify(adminPerformanceRepository).findAllByStatusOrderByCreatedAt(eq(PerformanceStatus.PENDING), eq(pageable));
//        verify(fileRepository).findAllById(anyList());
//        verify(userRepository).findAllById(anyList());
//        verify(adminPerformanceScheduleRepository).findByPerformanceIdIn(anyList());
//    }

    @Test
    @DisplayName("공연 승인 성공")
    void confirmPerformance_Success() {
        // given
        Long performanceId = 1L;

        Performance performance = mock(Performance.class);
        when(performance.isPending()).thenReturn(true);

        // when
        when(adminPerformanceRepository.findById(performanceId))
                .thenReturn(Optional.of(performance));

        adminPerformanceService.confirmPerformance(performanceId);

        // then
        verify(adminPerformanceRepository).findById(performanceId);
        verify(performance).confirm();
    }

    @Test
    @DisplayName("존재하지 않는 공연 승인 시도")
    void confirmPerformance_NotFound() {
        // given
        Long performanceId = 1L;

        // when
        when(adminPerformanceRepository.findById(performanceId))
                .thenReturn(Optional.empty());

        // then
        AppException exception = assertThrows(AppException.class, () ->
                adminPerformanceService.confirmPerformance(performanceId)
        );

        assertEquals(ErrorCode.PERFORMANCE_NOT_FOUND, exception.getErrorCode());
        verify(adminPerformanceRepository).findById(performanceId);
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 공연 승인 시도")
    void confirmPerformance_NotPending() {
        // given
        Long performanceId = 1L;

        Performance performance = mock(Performance.class);
        when(performance.isPending()).thenReturn(false);

        // when
        when(adminPerformanceRepository.findById(performanceId))
                .thenReturn(Optional.of(performance));

        // then
        AppException exception = assertThrows(AppException.class, () ->
                adminPerformanceService.confirmPerformance(performanceId)
        );

        assertEquals(ErrorCode.PERFORMANCE_STATUS_NOT_PENDING, exception.getErrorCode());
        verify(adminPerformanceRepository).findById(performanceId);
        verify(performance, never()).confirm();
    }

    @Test
    @DisplayName("공연 거부 성공")
    void rejectPerformance_Success() {
        // given
        Long performanceId = 1L;

        Performance performance = mock(Performance.class);
        when(performance.isPending()).thenReturn(true);

        // when
        when(adminPerformanceRepository.findById(performanceId))
                .thenReturn(Optional.of(performance));

        adminPerformanceService.rejectPerformance(performanceId);

        // then
        verify(adminPerformanceRepository).findById(performanceId);
        verify(performance).reject();
    }

    @Test
    @DisplayName("PENDING 상태의 매니저 요청 목록 조회 성공")
    void getPendingManagerRequestList_Success() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // 매니저 요청 엔티티 생성
        ManagerRequest managerRequest = ManagerRequest.builder()
                .id(1L)
                .userId(1L)
                .status(ManagerRequestStatus.PENDING)
                .approvedAt(null)
                .build();

        // 사용자 생성
        User user = User.builder()
                .id(1L)
                .email("test@test.com")
                .name("테스트 사용자")
                .phoneNumber("010-1234-5678")
                .role(Role.USER)
                .build();

        // 응답 DTO 생성
        PendingManagerRequestPageResponse expectedResponse = new PendingManagerRequestPageResponse(
                1L,
                1L,
                "테스트 사용자",
                "010-1234-5678"
        );

        // when
        when(adminManagerRequestRepository.findAllByStatusOrderByCreatedAt(eq(ManagerRequestStatus.PENDING), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(managerRequest)));
        when(adminUserRepository.findAllById(anyList()))
                .thenReturn(List.of(user));

        // static 메서드 모킹
        try (MockedStatic<AdminManagerRequestMapper> mockedMapper = Mockito.mockStatic(AdminManagerRequestMapper.class)) {
            mockedMapper.when(() -> AdminManagerRequestMapper.toPendingResponse(
                            any(ManagerRequest.class),
                            any(User.class)))
                    .thenReturn(expectedResponse);

            Page<PendingManagerRequestPageResponse> result = adminPerformanceService.getPendingManagerRequestList(pageable);

            // then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(expectedResponse, result.getContent().get(0));
        }

        verify(adminManagerRequestRepository).findAllByStatusOrderByCreatedAt(eq(ManagerRequestStatus.PENDING), eq(pageable));
        verify(adminUserRepository).findAllById(anyList());
    }

    @Test
    @DisplayName("매니저 요청 승인 성공")
    void approveManagerRequest_Success() {
        // given
        Long managerRequestId = 1L;
        Long userId = 1L;

        ManagerRequest managerRequest = mock(ManagerRequest.class);
        when(managerRequest.isPending()).thenReturn(true);
        when(managerRequest.getUserId()).thenReturn(userId);

        User user = mock(User.class);

        // when
        when(adminManagerRequestRepository.findById(managerRequestId))
                .thenReturn(Optional.of(managerRequest));
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        adminPerformanceService.approveManagerRequest(managerRequestId);

        // then
        verify(adminManagerRequestRepository).findById(managerRequestId);
        verify(userRepository).findById(userId);
        verify(user).promoteManager();
        verify(managerRequest).approve();
    }

    @Test
    @DisplayName("존재하지 않는 매니저 요청 승인 시도")
    void approveManagerRequest_NotFound() {
        // given
        Long managerRequestId = 1L;

        // when
        when(adminManagerRequestRepository.findById(managerRequestId))
                .thenReturn(Optional.empty());

        // then
        AppException exception = assertThrows(AppException.class, () ->
                adminPerformanceService.approveManagerRequest(managerRequestId)
        );

        assertEquals(ErrorCode.MANAGER_REQUEST_NOT_FOUND, exception.getErrorCode());
        verify(adminManagerRequestRepository).findById(managerRequestId);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("PENDING 상태가 아닌 매니저 요청 승인 시도")
    void approveManagerRequest_NotPending() {
        // given
        Long managerRequestId = 1L;

        ManagerRequest managerRequest = mock(ManagerRequest.class);
        when(managerRequest.isPending()).thenReturn(false);

        // when
        when(adminManagerRequestRepository.findById(managerRequestId))
                .thenReturn(Optional.of(managerRequest));

        // then
        AppException exception = assertThrows(AppException.class, () ->
                adminPerformanceService.approveManagerRequest(managerRequestId)
        );

        assertEquals(ErrorCode.MANAGER_REQUEST_STATUS_NOT_PENDING, exception.getErrorCode());
        verify(adminManagerRequestRepository).findById(managerRequestId);
        verify(managerRequest, never()).approve();
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("요청자 사용자 정보를 찾을 수 없는 경우")
    void approveManagerRequest_UserNotFound() {
        // given
        Long managerRequestId = 1L;
        Long userId = 1L;

        ManagerRequest managerRequest = mock(ManagerRequest.class);
        when(managerRequest.isPending()).thenReturn(true);
        when(managerRequest.getUserId()).thenReturn(userId);

        // when
        when(adminManagerRequestRepository.findById(managerRequestId))
                .thenReturn(Optional.of(managerRequest));
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // then
        AppException exception = assertThrows(AppException.class, () ->
                adminPerformanceService.approveManagerRequest(managerRequestId)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(adminManagerRequestRepository).findById(managerRequestId);
        verify(userRepository).findById(userId);
        verify(managerRequest, never()).approve();
    }

    @Test
    @DisplayName("매니저 요청 거부 성공")
    void rejectManagerRequest_Success() {
        // given
        Long managerRequestId = 1L;
        Long userId = 1L;

        ManagerRequest managerRequest = mock(ManagerRequest.class);
        when(managerRequest.isPending()).thenReturn(true);

        // when
        when(adminManagerRequestRepository.findById(managerRequestId))
                .thenReturn(Optional.of(managerRequest));

        adminPerformanceService.rejectManagerRequest(managerRequestId);

        // then
        verify(adminManagerRequestRepository).findById(managerRequestId);
        verify(managerRequest).reject();
        // 거부 시에는 사용자 권한을 변경하지 않으므로 userRepository를 호출하지 않음
        verify(userRepository, never()).findById(anyLong());
    }
}
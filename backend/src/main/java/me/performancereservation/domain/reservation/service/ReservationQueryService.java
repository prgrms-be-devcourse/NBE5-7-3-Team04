package me.performancereservation.domain.reservation.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.file.FileRepository;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.model.SchedulePerformanceInfo;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.domain.reservation.Reservation;
import me.performancereservation.domain.reservation.ReservationRepository;
import me.performancereservation.domain.reservation.dto.ReservationDetailResponse;
import me.performancereservation.domain.reservation.dto.ReservationPageResponse;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import me.performancereservation.domain.reservation.mapper.ReservationMapper;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationQueryService {
    private final ReservationRepository reservationRepository;
    private final PerformanceScheduleRepository performanceScheduleRepository;
    private final PerformanceRepository performanceRepository;
    private final FileRepository fileRepository;

    private final ReservationMapper reservationMapper;

    /**
     * 유저 id로 본인의 예약 정보를 전부 조회
     *
     * @param userId 유저 ID
     * @return 페이지네이션된 ReservationListResponse
     */
    @Transactional(readOnly = true)
    public Page<ReservationPageResponse> getAllByUserId(Long userId, Pageable pageable) {
        // 본인의 모든 예약 목록 조회
        Page<Reservation> reservations = reservationRepository.findAllByUserId(userId, pageable);

        // 공연 회차 ID 리스트 수집
        List<Long> scheduleIds = reservations.stream()
                .map(Reservation::getScheduleId)
                .toList();

        // in절로 SchedulePerformanceInfo 리스트 조회
        List<SchedulePerformanceInfo> scheduleInfos = performanceScheduleRepository.findAllSchedulePerformanceInfoByScheduleIds(scheduleIds);

        Map<Long, SchedulePerformanceInfo> infoMap = scheduleInfos.stream()
                .collect(Collectors.toMap(SchedulePerformanceInfo::scheduleId, sp -> sp));

        // Page<ReservationListResponseDto>로 매핑
        return reservations.map(reservation -> mapToResponse(reservation, infoMap));
    }

    // ReservationListResponseDto로 매핑 (가독성을 위해 map 로직 추출)
    private ReservationPageResponse mapToResponse(Reservation reservation, Map<Long, SchedulePerformanceInfo> infoMap) {
        SchedulePerformanceInfo schedulePerformanceInfo = infoMap.get(reservation.getScheduleId());

        return reservationMapper.toListResponseDto(reservation, schedulePerformanceInfo);
    }

    /**
     * 예약 id로 본인의 상세 예약 정보 조회
     *
     * @param reservationId 예약 ID
     * @param userId 로그인한 사용자의 ID
     * @return ReservationResponse
     */
    @Transactional(readOnly = true)
    public ReservationDetailResponse getByReservationId(Long reservationId, Long userId) {
        // 본인의 예약 조회
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ErrorCode.RESERVATION_NOT_FOUND.domainException("예약 ID: " + reservationId));

        // 본인의 예약이 아니면 예외
        if (!reservation.getUserId().equals(userId)) {
            throw ErrorCode.PERMISSION_DENIED.serviceException("해당 예약에 접근할 수 없습니다.");
        }

        // 공연 조회(메타 데이터 용)
        Performance performance = performanceRepository.findById(reservation.getPerformanceId())
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("공연 ID: " + reservation.getPerformanceId()));

        String fileUrl = null;

        if(performance.getFileId() != null) {
            fileUrl = fileRepository.findById(performance.getFileId())
                    .orElseThrow(() -> ErrorCode.FILE_NOT_FOUND.domainException("파일 ID: " + performance.getFileId())).getKey();
        }

        // SchedulePerformanceInfo 조회
        SchedulePerformanceInfo scheduleInfo = performanceScheduleRepository.findSchedulePerformanceInfoByScheduleId(reservation.getScheduleId())
                .orElseThrow(() -> ErrorCode.PERFORMANCE_SCHEDULE_NOT_FOUND.domainException("공연 회차 ID: " + reservation.getScheduleId()));

        // ReservationResponseDto 매핑
        return reservationMapper.toDetailResponseDto(reservation, scheduleInfo, performance, fileUrl);
    }
}

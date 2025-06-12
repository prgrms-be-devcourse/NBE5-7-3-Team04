package me.performancereservation.domain.performance.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.event.PerformanceScheduleCreatedEvent;
import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleRequest;
import me.performancereservation.domain.performance.entities.Performance;
import me.performancereservation.domain.performance.entities.PerformanceSchedule;
import me.performancereservation.domain.performance.event.ScheduleCanceledEvent;
import me.performancereservation.domain.performance.mapper.PerformanceScheduleMapper;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.repository.PerformanceScheduleRepository;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PerformanceScheduleService {

    private final PerformanceRepository performanceRepository;
    private final PerformanceScheduleRepository performanceScheduleRepository;
    private final ApplicationEventPublisher eventPublisher;

    /** 회차 등록
     *
     * 등록된 공연에 대한 회차를 등록
     * 공연 존재 여부를 검사(공연 승인 상태 검사)한 뒤 등록한 회차를 해당 공연에 연결
     * @param performanceId
     * @param request
     * @return performanceScheduleId
     */
    @Transactional
    public Long createPerformanceSchedule(Long performanceId, PerformanceScheduleRequest request, Long managerId) {
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + performanceId));

        // 권한 검사
        if(!performance.hasPermission(managerId)) {
            throw ErrorCode.PERMISSION_DENIED.domainException("등록 권한이 없습니다.");
        }

        // 해당 공연이 존재하고 관리자에게 승인을 받은 상태인지 확인
        if(!(performance.isConfirmed())) {
            throw ErrorCode.PERFORMANCE_PENDING_APPROVAL
                    .domainException("performanceId=" + performanceId + "는 승인 대기 상태");
        }

        // 회차 등록 가능 날짜 유효성 검사
        if(!(performance.isRegistrationPeriod(request.startTime(), request.endTime()))) {
            throw ErrorCode.INVALID_SCHEDULE_PERIOD.domainException("유효하지 않은 등록 기간입니다.");
        }

        PerformanceSchedule schedule = PerformanceScheduleMapper.toEntity(request, performanceId, performance.getTotalSeats());
        Long savedId = performanceScheduleRepository.save(schedule).getId();
        // 레디스 좌석 초기화 이벤트 호출
        eventPublisher.publishEvent(new PerformanceScheduleCreatedEvent(savedId, performance.getTotalSeats()));

        return savedId;
    }


    /** 특정 회차 공연 취소
     *
     * 취소 후 회차 id 반환
     * @param performanceScheduleId
     * @return scheduleId
     */
    @Transactional
    public Long cancelPerformanceSchedule(Long performanceId, Long performanceScheduleId, Long managerId) {
        // 취소할 회차 검색
        PerformanceSchedule schedule = performanceScheduleRepository.findById(performanceScheduleId)
                .orElseThrow(() -> ErrorCode.PERFORMANCE_SCHEDULE_NOT_FOUND.domainException("해당하는 공연 회차를 찾을 수 없습니다. id=" + performanceScheduleId));

        // 회차가 속한 공연 검색
        Performance performance = performanceRepository.findById(performanceId)
                .orElseThrow(() -> ErrorCode.PERFORMANCE_NOT_FOUND.domainException("해당하는 공연을 찾을 수 없습니다. id=" + performanceId));

        // 공연에 대한 접근 권한 검사
        if(!performance.hasPermission(managerId)) {
            throw ErrorCode.PERMISSION_DENIED.domainException("공연에 대한 권한이 없습니다.");
        }

        // 회차에 대한 접근 권한 검사 - 해당 공연의 회차가 맞는지
        if(!schedule.hasPermission(performance.getId())) {
            throw ErrorCode.PERMISSION_DENIED.domainException("회차에 대한 권한이 없습니다.");
        }

        schedule.cancel();
        // 예약 취소 이벤트 호출 처리
        eventPublisher.publishEvent(new ScheduleCanceledEvent(schedule.getId()));
        return schedule.getId();
    }
}

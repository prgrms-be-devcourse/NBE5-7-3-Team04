package me.performancereservation.api;


import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.file.FileService;
import me.performancereservation.domain.performance.dto.performance.request.PerformanceCreateRequest;
import me.performancereservation.domain.performance.dto.performance.request.PerformanceUpdateRequest;
import me.performancereservation.domain.performance.dto.performance.response.PerformanceManagerDetailResponse;
import me.performancereservation.domain.performance.dto.performance.response.PerformanceManagerPageResponse;
import me.performancereservation.domain.performance.dto.performance.response.PerformanceManagerListResponse;
import me.performancereservation.domain.performance.enums.PerformanceStatus;
import me.performancereservation.domain.performance.service.PerformanceScheduleService;
import me.performancereservation.domain.performance.service.PerformanceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/managers")
@RequiredArgsConstructor
public class ManagerPerformanceController {
    private final PerformanceService performanceService;
    private final PerformanceScheduleService performanceScheduleService;

    /** 공연자 자신의 공연 목록 페이지 호출
     *
     * 시큐리티 개발후 인증 시스템에서 매니저 Id 추출하는 방향으로 수정 필요
     * @param pageable
     * @param managerId
     * @return 200 + Page<PerformanceManagerListResponse>
     */
    @GetMapping("/performances")
    public ResponseEntity<Page<PerformanceManagerPageResponse>> getPerformances(
            @PageableDefault(
                    size=10,
                    sort = "performanceDate",
                    direction = Sort.Direction.DESC) Pageable pageable,
            Long managerId
//            @AuthenticationPrincipal CustomOAuth2User principal
    ) {


        //        Long managerId = principal.getUser().getId();
        Page<PerformanceManagerPageResponse> PerformanceManagerPageResponse = performanceService.getPerformanceManagerList(pageable, managerId);
        return ResponseEntity.ok(PerformanceManagerPageResponse);
    }

    /** 공연자 공연 상세 페이지
     *
     * 공연 기본 정보(아이디, 파일경로, 제목, 장소, 상태, 총 좌석수) + 회차 정보(아이디, 시작시간, 종료시간, 남은 좌석, 취소 여부)
     * @param performanceId
     * @param managerId
     * @return 200 + performanceManagerResponse
     */
    @GetMapping("/performances/{performanceId}")
    public ResponseEntity<PerformanceManagerDetailResponse> getPerformanceDetails(@PathVariable Long performanceId, Long managerId
//                                                                                  @AuthenticationPrincipal CustomOAuth2User principal
                                                                                  ) {
        //        Long managerId = principal.getUser().getId();
        PerformanceManagerDetailResponse performanceManagerResponse = performanceService.getPerformanceManagerDetail(performanceId, managerId);
        return ResponseEntity.ok(performanceManagerResponse);
    }

    /** 공연자 공연 등록 호출
     *
     * 매니저 id 시큐리티 개발 후 수정 필요
     * @param request
     * @param managerId
     * @return 201 + performanceId
     */
    @PostMapping("/register")
    public ResponseEntity<Long> registerPerformance(
            @RequestBody PerformanceCreateRequest request,
            Long managerId
//            @AuthenticationPrincipal CustomOAuth2User principal
            ) {

//        Long managerId = principal.getUser().getId();
        Long performanceId = performanceService.createPerformance(request, managerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(performanceId);
    }

    /** 공연 수정 호출
     *
     * 공연자 아이디 인증 객체로 변경 필요
     * @param performanceId
     * @param request
     * @param managerId
     */
    @PatchMapping("/performance/{performanceId}")
    public ResponseEntity<Void> updatePerformance(@PathVariable Long performanceId,
                                                  @RequestBody PerformanceUpdateRequest request,
                                                  Long managerId
//                                                  @AuthenticationPrincipal CustomOAuth2User principal
                                                  ) {
        //        Long managerId = principal.getUser().getId();
        performanceService.updatePerformance(performanceId, request, managerId);
        return ResponseEntity.noContent().build();
    }

    /** 공연 전체 취소 호출
     *
     * 공연 상태 취소 변경 + 연관된 모든 회차 상태 취소 변경
     * 공연자 아이디 인증 객체로 변경 필요
     * @param performanceId
     * @param managerId
     */
    @PatchMapping("/performances/{performanceId}/cancel")
    public ResponseEntity<Void> cancelPerformance(@PathVariable Long performanceId, Long managerId
//                                                  @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        //        Long managerId = principal.getUser().getId();
        performanceService.cancelPerformance(performanceId, managerId);

        /*

            예약 취소 관련 로직...?

         */

        return ResponseEntity.noContent().build();
    }

    /** 회차 취소 호출
     *
     * 선택한 회차에 대한 단일 취소
     * 공연자 아이디 인증 객체로 변경 필요
     * @param performanceId
     * @param performanceScheduleId
     * @param managerId
     */
    @PatchMapping("/performances/{performanceId}/schedules/{performanceScheduleId}")
    public ResponseEntity<Void> cancelPerformanceSchedule(@PathVariable Long performanceId,
                                                          @PathVariable Long performanceScheduleId,
                                                          Long managerId
//                                                          @AuthenticationPrincipal CustomOAuth2User principal
    ) {
        //        Long managerId = principal.getUser().getId();
        performanceScheduleService.cancelPerformanceSchedule(performanceId, performanceScheduleId, managerId);
        /*

            예약 취소 관련 로직...?

         */
        return ResponseEntity.noContent().build();
    }

    /** 공연자 공연 검색
     *
     * 기본적인 공연 제목 + 날짜, 공연장 + 날짜 검색 가능
     * 상태 정보를 프론트에서 탭으로 넘기게 되면 사용자 경험이 좋을 것 같아
     * 공연자용 검색에는 상태별 필터링 추가
     * 프론트에 전체, 공연중, 공연 완료 탭을 두고 탭을 통해 상태별 필터링
     * @param title
     * @param venue
     * @param start
     * @param end
     * @param status
     * @param pageable
     * @param managerId
     * @return PerformanceManagerListResponse
     */
    @GetMapping("/performances/search")
    public ResponseEntity<Page<PerformanceManagerListResponse>> searchPerformances(@RequestParam(required = false) String title,
                                                                                   @RequestParam(required = false) String venue,
                                                                                   @RequestParam(required = false)  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                                                                                   @RequestParam(required = false) PerformanceStatus status,
                                                                                   Pageable pageable,
                                                                                   Long managerId
//                                                                                   @AuthenticationPrincipal CustomOAuth2User principal
                                                                                   ) {
        //        Long managerId = principal.getUser().getId();
        Page<PerformanceManagerListResponse> performanceManagerListResponses = performanceService.searchManagerPerformances(managerId, title, venue, start, end, status, pageable);
        return ResponseEntity.ok(performanceManagerListResponses);

    }
}

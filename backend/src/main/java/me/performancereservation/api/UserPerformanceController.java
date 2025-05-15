package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.dto.performance.response.PerformanceDetailResponse;
import me.performancereservation.domain.performance.dto.performance.response.PerformancePageResponse;
import me.performancereservation.domain.performance.enums.PerformanceCategory;
import me.performancereservation.domain.performance.service.PerformanceScheduleService;
import me.performancereservation.domain.performance.service.PerformanceService;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserPerformanceController {

    private final PerformanceService performanceService;
    private final PerformanceScheduleService performanceScheduleService;

    /** 메인화면 호출(공연목록 조회)
     *
     * @param pageable 10개 단위 페이징 + 최신 공연 내림차순
     *                 쿼리에서 바로 정렬하여 가져오도록 변경 (디폴트 페이징 정렬 설정 제거)
     * @return 200 + performanceResponses
     */
    @GetMapping
    public ResponseEntity<Page<PerformancePageResponse>> getPerformanceList(Pageable pageable) {
        Page<PerformancePageResponse> performancePageResponse = performanceService.getPerformanceList(pageable);
        return ResponseEntity.ok(performancePageResponse);
    }

    /** 공연 상세정보 페이지 호출
     *
     * @param performanceId
     * @return 200 + performanceResponse
     */
    @GetMapping("/performances/{performanceId}")
    public ResponseEntity<PerformanceDetailResponse> getPerformanceDetail(@PathVariable Long performanceId,
                                                                          @AuthenticationPrincipal CustomOAuth2User principal) {
        Long userId = null;
        if(principal != null) {
            userId = principal.getUser().getId();
        }

        PerformanceDetailResponse performanceResponse = performanceService.getPerformanceDetail(performanceId, userId);
        return ResponseEntity.ok(performanceResponse);
    }


    /** 공연 목록 검색
     *
     * @param title 제목
     * @param venue 공연장
     * @param start 필터링 시작 날짜
     * @param end 필터링 종료 날짜
     * @param pageable 페이징
     * @return performanceListResponses
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PerformancePageResponse>> searchPerformanceList(@RequestParam(required = false) String title,
                                                                               @RequestParam(required = false) String venue,
                                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
                                                                               @RequestParam(required = false) PerformanceCategory category,
                                                                               Pageable pageable
    ) {
        Page<PerformancePageResponse> performancePageResponse = performanceService.searchPerformances(title, venue, start, end, category, pageable);
        return ResponseEntity.ok(performancePageResponse);
    }

}

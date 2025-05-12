package me.performancereservation.api;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.performance.dto.performance.response.PerformanceDetailResponse;
import me.performancereservation.domain.performance.dto.performance.response.PerformanceListResponse;
import me.performancereservation.domain.performance.repository.PerformanceRepository;
import me.performancereservation.domain.performance.service.PerformanceScheduleService;
import me.performancereservation.domain.performance.service.PerformanceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserPerformanceController {

    private final PerformanceService performanceService;
    private final PerformanceScheduleService performanceScheduleService;

    /** 메인화면 호출(공연목록 조회)
     *
     * @param pageable 10개 단위 페이징 + 최신 공연 내림차순
     * @return 200 + performanceResponses
     */
    @GetMapping
    public ResponseEntity<Page<PerformanceListResponse>> getPerformanceList(
            @PageableDefault(size=10, sort = "performanceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PerformanceListResponse> performancePageResponse = performanceService.getPerformanceList(pageable);
        return ResponseEntity.ok(performancePageResponse);
    }

    /** 공연 상세정보 페이지 호출
     *
     * @param performanceId
     * @return 200 + performanceResponse
     */
    @GetMapping("/performances/{performanceId}")
    public ResponseEntity<PerformanceDetailResponse> getPerformanceDetail(@PathVariable Long performanceId) {
        PerformanceDetailResponse performanceResponse = performanceService.getPerformanceDetail(performanceId);
        return ResponseEntity.ok(performanceResponse);
    }

}

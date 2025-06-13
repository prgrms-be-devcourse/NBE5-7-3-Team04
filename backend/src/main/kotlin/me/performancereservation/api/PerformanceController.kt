package me.performancereservation.api

import jakarta.validation.Valid
import me.performancereservation.api.docs.ManagerPerformanceApiDocs
import me.performancereservation.api.docs.UserPerformanceApiDocs
import me.performancereservation.domain.performance.dto.performance.*
import me.performancereservation.domain.performance.dto.performanceschedule.PerformanceScheduleRequest
import me.performancereservation.domain.performance.enums.PerformanceCategory
import me.performancereservation.domain.performance.enums.PerformanceStatus
import me.performancereservation.domain.performance.service.PerformanceScheduleService
import me.performancereservation.domain.performance.service.PerformanceService
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/managers")
class ManagerPerformanceController (
    private val performanceService: PerformanceService,
    private val performanceScheduleService: PerformanceScheduleService
) : ManagerPerformanceApiDocs {


    /** 공연자 자신의 공연 목록 페이지 호출
     *
     * 시큐리티 개발후 인증 시스템에서 매니저 Id 추출하는 방향으로 수정 필요
     * @param pageable
     * @return 200 + Page<PerformanceManagerListResponse>
    </PerformanceManagerListResponse> */
    @GetMapping("/performances")
    override fun getPerformances(
        @PageableDefault(size = 10, sort = ["startDate"], direction = Sort.Direction.DESC) pageable: Pageable,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Page<PerformanceManagerPageResponse>> {
        val managerId = principal.user.id
        val performanceManagerPageResponse = performanceService.getPerformanceManagerList(pageable, managerId!!)
        return ResponseEntity.ok(performanceManagerPageResponse)
    }

    /** 공연자 공연 상세 페이지
     *
     * 공연 기본 정보(아이디, 파일경로, 제목, 장소, 상태, 총 좌석수) + 회차 정보(아이디, 시작시간, 종료시간, 남은 좌석, 취소 여부)
     * @param performanceId
     * @return 200 + performanceManagerResponse
     */
    @GetMapping("/performances/{performanceId}")
    override fun getPerformanceDetails(
        @PathVariable performanceId: Long,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<PerformanceManagerDetailResponse> {
        val managerId = principal.user.id
        val performanceManagerResponse = performanceService.getPerformanceManagerDetail(performanceId, managerId!!)
        return ResponseEntity.ok(performanceManagerResponse)
    }

    /** 공연자 공연 등록 호출
     *
     * 매니저 id 시큐리티 개발 후 수정 필요
     * @param request
     * @return 201 + performanceId
     */
    @PostMapping("/register")
    override fun registerPerformance(
        @Valid @RequestBody request:  PerformanceCreateRequest,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Long> {
        val managerId = principal.user.id
        val performanceId = performanceService.createPerformance(request, managerId!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(performanceId)
    }

    /** 공연자 회차 등록 호출
     *
     * @param performanceId
     * @param request
     * @return 201 + scheduleId
     */
    @PostMapping("/performances/{performanceId}/register")
    override fun registerPerformanceSchedule(
        @PathVariable performanceId: Long,
        @Valid @RequestBody request: PerformanceScheduleRequest,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Long> {
        val managerId = principal.user.id
        val scheduleId = performanceScheduleService.createPerformanceSchedule(performanceId, request, managerId!!)
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleId)
    }

    /** 공연 수정 호출
     *
     * 공연자 아이디 인증 객체로 변경 필요
     * @param performanceId
     * @param request
     */
    @PatchMapping("/performances/{performanceId}")
    override fun updatePerformance(
        @PathVariable performanceId: Long,
        @Valid @RequestBody request: PerformanceUpdateRequest,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void> {
        val managerId = principal.user.id
        performanceService.updatePerformance(performanceId, request, managerId!!)
        return ResponseEntity.noContent().build()
    }

    /** 공연 전체 취소 호출
     *
     * 공연 상태 취소 변경 + 연관된 모든 회차 상태 취소 변경
     * 공연자 아이디 인증 객체로 변경 필요
     * @param performanceId
     */
    @PatchMapping("/performances/{performanceId}/cancel")
    override fun cancelPerformance(
        @PathVariable performanceId: Long,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void> {
        val managerId = principal.user.id
        performanceService.cancelPerformance(performanceId, managerId!!)
        return ResponseEntity.noContent().build()
    }

    /** 회차 취소 호출
     *
     * 선택한 회차에 대한 단일 취소
     * 공연자 아이디 인증 객체로 변경 필요
     * @param performanceId
     * @param performanceScheduleId
     */
    @PatchMapping("/performances/{performanceId}/schedules/{performanceScheduleId}")
    override fun cancelPerformanceSchedule(
        @PathVariable performanceId: Long,
        @PathVariable performanceScheduleId: Long,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Void> {
        val managerId = principal.user.id
        performanceScheduleService.cancelPerformanceSchedule(performanceId, performanceScheduleId, managerId!!)
        return ResponseEntity.noContent().build()
    }

    /** 공연자 공연 검색
     *
     * 기본적인 공연 제목 + 날짜, 공연장 + 날짜 검색 가능
     * 상태 정보를 프론트에서 탭으로 넘기게 되면 사용자 경험이 좋을 것 같아
     * 공연자용 검색에는 상태별 필터링 추가
     * 프론트에 전체, 공연중, 공연 완료 탭을 두고 탭을 통해 상태별 필터링
     *
     * @param title
     * @param venue
     * @param start
     * @param end
     * @param status
     * @param pageable
     * @return PerformanceManagerListResponse
     */
    @GetMapping("/performances/search")
    override fun searchPerformances(
        @RequestParam(required = false) title: String,
        @RequestParam(required = false) venue: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: LocalDateTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: LocalDateTime,
        @RequestParam(required = false) status: PerformanceStatus,
        pageable: Pageable,
        @AuthenticationPrincipal principal: CustomOAuth2User
    ): ResponseEntity<Page<PerformanceManagerPageResponse>> {
        val managerId = principal.user.id
        val performanceManagerPageResponse =
            performanceService.searchManagerPerformances(managerId!!, title, venue, start, end, status, pageable)
        return ResponseEntity.ok(performanceManagerPageResponse)
    }
}

//--------------------------------- 유저 컨트롤러 ---------------------------------------

@RestController
@RequestMapping("/api/v1/users")
class UserPerformanceController (
    private val performanceService: PerformanceService,
    private val performanceScheduleService: PerformanceScheduleService
) : UserPerformanceApiDocs {

    /** 메인화면 호출(공연목록 조회)
     *
     * @param pageable 10개 단위 페이징 + 최신 공연 내림차순
     * 쿼리에서 바로 정렬하여 가져오도록 변경 (디폴트 페이징 정렬 설정 제거)
     * @return 200 + performanceResponses
     */
    @GetMapping("/performances")
    override fun getPerformanceList(pageable: Pageable): ResponseEntity<Page<PerformancePageResponse>> {
        val performancePageResponse = performanceService.getPerformanceList(pageable)
        return ResponseEntity.ok(performancePageResponse)
    }

    /** 공연 상세정보 페이지 호출
     *
     * @param performanceId
     * @return 200 + performanceResponse
     */
    @GetMapping("/performances/{performanceId}")
    override fun getPerformanceDetail(
        @PathVariable performanceId: Long,
        @AuthenticationPrincipal principal: CustomOAuth2User?
    ): ResponseEntity<PerformanceDetailResponse> {
        var userId: Long? = null
        if (principal != null) {
            userId = principal.user.id
        }

        val performanceResponse = performanceService.getPerformanceDetail(performanceId, userId)
        return ResponseEntity.ok(performanceResponse)
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
    override fun searchPerformanceList(
        @RequestParam(required = false) title: String,
        @RequestParam(required = false) venue: String,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: LocalDateTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: LocalDateTime,
        @RequestParam(required = false) category: PerformanceCategory,
        pageable: Pageable
    ): ResponseEntity<Page<PerformancePageResponse>> {
        val performancePageResponse =
            performanceService.searchPerformances(title, venue, start, end, category, pageable)
        return ResponseEntity.ok(performancePageResponse)
    }
}

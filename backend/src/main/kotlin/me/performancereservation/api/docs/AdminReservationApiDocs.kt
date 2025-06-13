package me.performancereservation.api.docs

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import me.performancereservation.domain.admin.dto.AdminReservationPageResponse
import me.performancereservation.domain.reservation.enums.ReservationStatus
import me.performancereservation.global.exception.ErrorResponse
import org.springdoc.core.annotations.ParameterObject
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

@Tag(name = "Admin Reservation API", description = "관리자용 예매 관련 API")
interface AdminReservationApiDocs {
    @Operation(summary = "예매 목록 조회", description = "모든 예매 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "403",
            description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    @PageableAsQueryParam
    fun getReservations(
        @ParameterObject pageable: Pageable
    ): ResponseEntity<Page<AdminReservationPageResponse>>

    @Operation(summary = "예매 검색", description = "회원 이름, 공연 제목, 예매 상태, 날짜 범위로 예매를 검색합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "200", description = "검색 성공"), ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "403",
            description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    @PageableAsQueryParam
    fun searchReservations(
        @ParameterObject pageable: Pageable,
        @Parameter(description = "회원 이름") userName: String?,
        @Parameter(description = "공연 제목") performanceTitle: String?,
        @Parameter(description = "예매 상태") reservationStatus: ReservationStatus?,
        @Parameter(description = "시작 날짜 (ISO 형식: yyyy-MM-dd'T'HH:mm:ss)") startDate: LocalDateTime?,
        @Parameter(description = "종료 날짜 (ISO 형식: yyyy-MM-dd'T'HH:mm:ss)") endDate: LocalDateTime?
    ): ResponseEntity<Page<AdminReservationPageResponse>>

    @Operation(summary = "예매 확정", description = "대기 중인 예매를 확정 상태로 변경합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "확정 성공"),
            ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "403",
            description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "404",
            description = "예매를 찾을 수 없음 (RESERVATION_NOT_FOUND) 또는 회차를 찾을 수 없음 (PERFORMANCE_SCHEDULE_NOT_FOUND)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    fun confirmReservation(
        @Parameter(description = "예매 ID", required = true) reservationId: Long
    ): ResponseEntity<Void>

    @Operation(summary = "공연별 예매 일괄 취소", description = "특정 공연의 모든 예매를 일괄 취소합니다.")
    @ApiResponses(
        value = [ApiResponse(responseCode = "204", description = "취소 성공"), ApiResponse(
            responseCode = "401",
            description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "403",
            description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        ), ApiResponse(
            responseCode = "404",
            description = "공연을 찾을 수 없음 (PERFORMANCE_NOT_FOUND)",
            content = [Content(schema = Schema(implementation = ErrorResponse::class))]
        )]
    )
    fun bulkCancelByPerformanceId(
        @Parameter(description = "공연 ID", required = true) performanceId: Long
    ): ResponseEntity<Void>
}
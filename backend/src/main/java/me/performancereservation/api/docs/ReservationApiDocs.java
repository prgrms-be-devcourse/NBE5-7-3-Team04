package me.performancereservation.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.performancereservation.domain.reservation.dto.ReservationDetailResponse;
import me.performancereservation.domain.reservation.dto.ReservationPageResponse;
import me.performancereservation.domain.reservation.dto.ReservationRequest;
import me.performancereservation.domain.reservation.dto.ReservationResponse;
import me.performancereservation.global.exception.ErrorResponse;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Map;

@Tag(name = "Reservation API", description = "예매 관련 API")
public interface ReservationApiDocs {

    @Operation(summary = "공연 예매", description = "공연을 예매합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "예매 성공",
            content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (INVALID_REQUEST)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "공연 일정을 찾을 수 없음 (PERFORMANCE_SCHEDULE_NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "409", description = "좌석 수량 부족 (INSUFFICIENT_SEATS)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<ReservationResponse> reserve(
        @Parameter(description = "예매 정보", required = true) ReservationRequest request,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User authentication
    );

    @Operation(summary = "예매 취소", description = "예매를 취소합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "취소 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "403", description = "권한 없음 (PERMISSION_DENIED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "예매를 찾을 수 없음 (RESERVATION_NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "409", description = "이미 취소된 예매 (RESERVATION_ALREADY_CANCELLED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<Map<String, Long>> cancel(
        @Parameter(description = "예매 ID", required = true) Long reservationId,
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User authentication
    );

    @Operation(summary = "예매 목록 조회", description = "사용자의 모든 예매 내역을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PageableAsQueryParam
    ResponseEntity<Page<ReservationPageResponse>> getUserReservations(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User authentication,
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "예매 상세 조회", description = "특정 예매의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = ReservationResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "403", description = "권한 없음 (PERMISSION_DENIED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "예매를 찾을 수 없음 (RESERVATION_NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<ReservationDetailResponse> getReservationById(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User authentication,
        @Parameter(description = "예매 ID", required = true) Long reservationId
    );
} 
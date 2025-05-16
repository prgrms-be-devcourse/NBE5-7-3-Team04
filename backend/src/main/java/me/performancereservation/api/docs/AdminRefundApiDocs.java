package me.performancereservation.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.global.exception.ErrorResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin Refund API", description = "관리자용 환불 관련 API")
public interface AdminRefundApiDocs {

    @Operation(summary = "환불 내역 조회", description = "모든 환불 내역을 조회합니다. 상태별 필터링이 가능합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "403", description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
    })
    @PageableAsQueryParam
    ResponseEntity<Page<RefundDetailResponse>> getAllRefundDetailsByRefundStatus(
        @Parameter(description = "환불 상태 (선택사항)") String status,
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "환불 승인", description = "대기 중인 환불을 승인합니다. 예약 상태도 자동으로 변경됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "승인 성공"),
        @ApiResponse(responseCode = "400", description = "PENDING 상태가 아닌 환불 (REFUND_STATUS_NOT_PENDING)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "403", description = "관리자 권한이 없음 (UNAUTHORIZED_ADMIN)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "404", description = "환불 내역을 찾을 수 없음 (REFUND_NOT_FOUND)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<Void> confirmRefund(
        @Parameter(description = "환불 ID", required = true) Long refundId
    );
} 
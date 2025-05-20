package me.performancereservation.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.performancereservation.domain.refund.dto.RefundDetailResponse;
import me.performancereservation.domain.refund.dto.UpdateBankInfoRequest;
import me.performancereservation.global.exception.ErrorResponse;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Refund API", description = "환불 관련 API")
public interface RefundApiDocs {

    @Operation(summary = "환불 내역 조회", description = "사용자의 모든 환불 내역을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PageableAsQueryParam
    ResponseEntity<Page<RefundDetailResponse>> getAllRefundDetailsWithUserId(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User authentication,
        @ParameterObject Pageable pageable
    );

    @Operation(summary = "환불 계좌 정보 수정", description = "환불을 받을 계좌 정보를 수정합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<Void> updateBankInfo(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User authentication,
        @Parameter(description = "계좌 정보", required = true) UpdateBankInfoRequest request
    );
} 
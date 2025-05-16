package me.performancereservation.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.performancereservation.domain.settlement.dto.SettlementRequest;
import me.performancereservation.domain.settlement.dto.SettlementResponse;
import me.performancereservation.global.exception.ErrorResponse;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springdoc.core.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Settlement API", description = "정산 관련 API")
public interface SettlementApiDocs {

    @Operation(summary = "정산 생성", description = "공연관리자가 정산을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (INVALID_REQUEST)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "403", description = "권한 없음 (PERMISSION_DENIED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<Long> createSettlement(
        @Parameter(description = "정산 생성 정보", required = true) SettlementRequest request
    );

    @Operation(summary = "정산 내역 조회", description = "공연관리자의 정산 내역을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "403", description = "권한 없음 (PERMISSION_DENIED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PageableAsQueryParam
    ResponseEntity<Page<SettlementResponse>> getAllSettlementsWithUserId(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User authentication,
        @ParameterObject Pageable pageable
    );
} 
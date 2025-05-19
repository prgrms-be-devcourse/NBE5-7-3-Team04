package me.performancereservation.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.performancereservation.domain.user.dto.UserOnboardingRequest;
import me.performancereservation.domain.user.dto.UserResponse;
import me.performancereservation.global.exception.ErrorResponse;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "User API", description = "사용자 관련 API")
public interface UserApiDocs {

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<UserResponse> getMyInfo(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User principal
    );

    @Operation(summary = "공연자 권한 확인", description = "공연자 권한을 확인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "확인 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<Boolean> canRequestManagerRole(
            @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User principal
    );

    @Operation(summary = "공연자 권한 요청", description = "공연자 권한을 요청합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "요청 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "409", description = "이미 공연자 권한 요청 중 (MANAGER_REQUEST_ALREADY_EXISTS)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<Void> submitManagerRequest(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User principal
    );

    @Operation(summary = "온보딩 정보 등록", description = "사용자의 온보딩 정보를 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (INVALID_REQUEST)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자 (UNAUTHORIZED)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<UserResponse> onboard(
        @Parameter(description = "인증된 사용자 정보") @AuthenticationPrincipal CustomOAuth2User principal,
        @Parameter(description = "온보딩 정보", required = true) UserOnboardingRequest request
    );
} 
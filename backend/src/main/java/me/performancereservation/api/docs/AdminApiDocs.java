package me.performancereservation.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin API", description = "관리자 관련 API")
public interface AdminApiDocs {

    @Operation(summary = "관리자 권한 테스트", description = "현재 로그인한 사용자의 관리자 권한을 확인합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "권한 확인 성공"),
        @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        @ApiResponse(responseCode = "403", description = "권한이 없는 사용자")
    })
    ResponseEntity<Object> authTest();
} 
package me.performancereservation.api.docs;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.performancereservation.domain.file.dto.UploadFileResponse;
import me.performancereservation.global.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "File API", description = "파일 업로드 관련 API")
public interface FileApiDocs {

    @Operation(summary = "파일 업로드", description = "파일을 업로드하고 업로드된 파일의 정보를 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "업로드 성공",
            content = @Content(schema = @Schema(implementation = UploadFileResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(responseCode = "413", description = "파일 크기 초과",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    ResponseEntity<UploadFileResponse> uploadFile(
        @Parameter(description = "업로드할 파일", required = true) MultipartFile file
    );
} 
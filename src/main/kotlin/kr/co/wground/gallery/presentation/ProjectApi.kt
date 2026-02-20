package kr.co.wground.gallery.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import kr.co.wground.gallery.presentation.request.CreateProjectRequest
import kr.co.wground.gallery.presentation.request.UpdateProjectRequest
import kr.co.wground.gallery.presentation.response.ProjectDetailResponse
import kr.co.wground.gallery.presentation.response.ProjectSummaryPageResponse
import kr.co.wground.gallery.presentation.response.ProjectTrackFiltersResponse
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Project", description = "프로젝트 갤러리 API")
interface ProjectApi {

    @Operation(summary = "프로젝트 생성", description = "새로운 프로젝트를 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                headers = [
                    Header(
                        name = "Location",
                        description = "생성된 프로젝트 리소스 URI",
                        schema = Schema(type = "string", example = "/api/v1/projects/10")
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ]
    )
    fun createProject(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            schema = Schema(type = "string")
        ) userId: CurrentUserId,
        @Valid @RequestPart("data") request: CreateProjectRequest,
        @RequestPart("thumbnailImage") thumbnailImage: MultipartFile,
    ): ResponseEntity<Unit>

    @Operation(summary = "프로젝트 목록 조회", description = "갤러리 목록을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProjectSummaryPageResponse::class)
                )]
            ),
        ]
    )
    fun getProjects(
        @RequestParam(required = false) trackId: TrackId?,
        @RequestParam(required = false) keyword: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "12") size: Int,
        @RequestParam(defaultValue = "createdAt,desc") sort: String,
        @Parameter(`in` = ParameterIn.COOKIE, name = "accessToken", schema = Schema(type = "string"))
        user: CurrentUserId,
    ): ResponseEntity<ProjectSummaryPageResponse>

    @Operation(summary = "트랙 필터 목록 조회", description = "프로젝트가 존재하는 트랙 목록을 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProjectTrackFiltersResponse::class)
                )]
            ),
        ]
    )
    fun getTrackFilters(): ResponseEntity<ProjectTrackFiltersResponse>

    @Operation(summary = "프로젝트 상세 조회", description = "프로젝트 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ProjectDetailResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "프로젝트 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class)
                )]
            ),
        ]
    )
    fun getProject(
        @Parameter(description = "프로젝트 ID") projectId: ProjectId,
        @Parameter(`in` = ParameterIn.COOKIE, name = "accessToken", schema = Schema(type = "string"))
        user: CurrentUserId,
    ): ResponseEntity<ProjectDetailResponse>

    @Operation(summary = "프로젝트 수정", description = "제공된 필드만 부분 수정합니다. 등록자 본인 또는 ADMIN만 가능합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(
                responseCode = "403", description = "권한 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "프로젝트 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
        ]
    )
    fun updateProject(
        @Parameter(description = "프로젝트 ID") projectId: ProjectId,
        @Parameter(`in` = ParameterIn.COOKIE, name = "accessToken", schema = Schema(type = "string"))
        userId: CurrentUserId,
        @Valid @RequestPart("data") request: UpdateProjectRequest,
        @RequestPart(name = "thumbnailImage", required = false) thumbnailImage: MultipartFile?,
    ): ResponseEntity<Unit>

    @Operation(summary = "프로젝트 삭제", description = "프로젝트를 삭제합니다. 등록자 본인 또는 ADMIN만 가능합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(
                responseCode = "403", description = "권한 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "프로젝트 없음",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
            ),
        ]
    )
    fun deleteProject(
        @Parameter(description = "프로젝트 ID") projectId: ProjectId,
        @Parameter(`in` = ParameterIn.COOKIE, name = "accessToken", schema = Schema(type = "string"))
        userId: CurrentUserId,
    ): ResponseEntity<Unit>
}

package kr.co.wground.post.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.post.docs.SwaggerErrorExample
import kr.co.wground.post.docs.SwaggerErrorExample.InvalidArgument
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.post.presentation.request.PostCreateRequest
import kr.co.wground.post.presentation.request.PostUpdateRequest
import kr.co.wground.post.presentation.response.PostDetailResponse
import kr.co.wground.post.presentation.response.PostSummaryResponse
import org.springdoc.core.annotations.ParameterObject
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity

@Tag(name = "Posts", description = "게시글 API")
interface PostApi {

    @Operation(summary = "게시글 생성", description = "title/body로 게시글을 생성합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "생성 성공",
                headers = [
                    Header(
                        name = "Location",
                        description = "생성된 게시글 리소스 URI",
                        schema = Schema(type = "string", example = "/api/v1/posts/1")
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400", description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "INVALID_INPUT", value = InvalidArgument.VALIDATION)
                    ]
                )]
            )]
    )
    fun writePost(request: PostCreateRequest, writer: CurrentUserId): ResponseEntity<Unit>

    @Operation(summary = "게시글 삭제", description = "게시글 id로 게시글을 삭제합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "NOT_FOUND_POST", value = SwaggerErrorExample.NotFound.NOT_FOUND_POST)]
                )]
            )
        ]
    )
    fun deletePost(@Schema(example = "1")id: PostId, writer: CurrentUserId): ResponseEntity<Unit>

    @Operation(summary = "게시글 수정", description = "id에 맞는 게시글을 수정합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "수정 성공"),
            ApiResponse(
                responseCode = "400", description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "INVALID_INPUT", value = InvalidArgument.VALIDATION)
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "403", description = "게시글의 주인이 아님",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "YOU_ARE_NOT_OWNER_THIS_POST", value = SwaggerErrorExample.FORBIDDEN.YOU_ARE_NOT_OWNER_THIS_POST)]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(name = "NOT_FOUND_POST", value = SwaggerErrorExample.NotFound.NOT_FOUND_POST)]
                )]
            ),
        ]
    )
    fun updatePost(
        @Schema(example = "1") id: PostId, request: PostUpdateRequest, writer: CurrentUserId
    ): ResponseEntity<Unit>

    @Operation(summary = "게시글을 page, size 단위로 summary들만 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ]
    )
    @PageableAsQueryParam
    fun getPostSummary(
        @ParameterObject @PageableDefault(size = 20) pageable: Pageable,
        @Parameter(
            description = "토픽 필터(없으면 전체)",
            example = "NOTICE"
        )
        topic: Topic?,
        userId: CurrentUserId
    ): PostSummaryResponse

    @Operation(summary = "게시글의 id로 상세 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(
                responseCode = "404", description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "NOT_FOUND_POST", value = SwaggerErrorExample.NotFound.NOT_FOUND_POST),
                        ExampleObject(name = "NOT_FOUND_WRITER", value = SwaggerErrorExample.NotFound.NOT_FOUND_WRITER)
                    ]
                )]
            ),
        ]
    )
    fun getPost(@Schema(example = "1") id: PostId): PostDetailResponse

    @Operation(summary = "page, size 단위로 내가쓴 게시글을 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
        ]
    )
    fun getMyPost(
        @ParameterObject @PageableDefault(size = 20)
        pageable: Pageable,
        userId: CurrentUserId
    ): PostSummaryResponse
}

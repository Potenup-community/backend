package kr.co.wground.comment.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.comment.docs.CommentSwaggerErrorExample
import kr.co.wground.comment.docs.CommentSwaggerResponseExample
import kr.co.wground.comment.presentation.request.CommentCreateRequest
import kr.co.wground.comment.presentation.request.CommentUpdateRequest
import kr.co.wground.comment.presentation.response.CommentSummaryResponse
import kr.co.wground.comment.presentation.response.CommentsResponse
import kr.co.wground.comment.presentation.response.LikedCommentsResponse
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity

@Tag(name = "Comments", description = "댓글 API")
interface CommentApi {

    @Operation(
        summary = "댓글 작성",
        description = "게시글 ID와 선택적인 부모 댓글 ID, 본문 내용으로 댓글을 작성합니다. parentId가 없으면 일반 댓글, 있으면 대댓글로 저장됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "생성 성공",
                headers = [
                    Header(
                        name = "Location",
                        description = "생성된 댓글 리소스 URI",
                        schema = Schema(type = "string", example = "/api/v1/comments/10")
                    )
                ]
            ),
            ApiResponse(
                responseCode = "400",
                description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "INVALID_INPUT_COMMON",
                            value = CommentSwaggerErrorExample.Common.INVALID_INPUT_CONTENT
                        ),
                        ExampleObject(
                            name = "INVALID_INPUT",
                            value = CommentSwaggerErrorExample.BadRequest.INVALID_INPUT
                        ),
                        ExampleObject(
                            name = "EMPTY_CONTENT",
                            value = CommentSwaggerErrorExample.BadRequest.EMPTY_CONTENT
                        ),
                        ExampleObject(
                            name = "INVALID_REPLY",
                            value = CommentSwaggerErrorExample.BadRequest.INVALID_REPLY
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "대상 게시글 혹은 부모 댓글을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "TARGET_POST",
                            value = CommentSwaggerErrorExample.NotFound.TARGET_POST
                        ),
                        ExampleObject(
                            name = "PARENT_COMMENT",
                            value = CommentSwaggerErrorExample.NotFound.COMMENT_PARENT
                        ),
                    ]
                )]
            ),
        ]
    )
    fun writeComment(
        request: CommentCreateRequest,
        @Parameter(description = "현재 로그인한 사용자 ID", hidden = true) writerId: CurrentUserId,
    ): ResponseEntity<Unit>

    @Operation(
        summary = "댓글 수정",
        description = "댓글 ID로 댓글 본문을 수정합니다. 작성자 본인만 수정할 수 있으며 content가 null이면 기존 내용을 유지합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "수정 성공"),
            ApiResponse(
                responseCode = "400",
                description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "INVALID_INPUT_COMMON",
                            value = CommentSwaggerErrorExample.Common.INVALID_INPUT_CONTENT
                        ),
                        ExampleObject(
                            name = "INVALID_INPUT",
                            value = CommentSwaggerErrorExample.BadRequest.INVALID_INPUT
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "403",
                description = "댓글 작성자가 아님",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_WRITER",
                            value = CommentSwaggerErrorExample.Forbidden.NOT_WRITER
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "댓글을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "COMMENT_NOT_FOUND",
                            value = CommentSwaggerErrorExample.NotFound.COMMENT
                        )
                    ]
                )]
            ),
        ]
    )
    fun updateComment(
        @Parameter(description = "수정할 댓글 ID", example = "1") id: CommentId,
        request: CommentUpdateRequest,
        @Parameter(description = "현재 로그인한 사용자 ID", hidden = true) writerId: CurrentUserId,
    ): ResponseEntity<Unit>

    @Operation(
        summary = "댓글 삭제",
        description = "댓글 ID로 댓글을 삭제합니다. 작성자 본인만 삭제 가능하며 내용이 블라인드 처리됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "삭제 성공"),
            ApiResponse(
                responseCode = "403",
                description = "댓글 작성자가 아님",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_WRITER",
                            value = CommentSwaggerErrorExample.Forbidden.NOT_WRITER
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "댓글을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "COMMENT_NOT_FOUND",
                            value = CommentSwaggerErrorExample.NotFound.COMMENT
                        )
                    ]
                )]
            ),
        ]
    )
    fun deleteComment(
        @Parameter(description = "삭제할 댓글 ID", example = "1") id: CommentId,
        @Parameter(description = "현재 로그인한 사용자 ID", hidden = true) writerId: CurrentUserId,
    ): ResponseEntity<Unit>

    @Operation(
        summary = "게시글 댓글 조회",
        description = "게시글 ID로 댓글/대댓글을 트리 형태로 모두 조회합니다. 로그인 사용자가 누른 반응 여부와 통계가 함께 반환됩니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CommentsResponse::class),
                    examples = [
                        ExampleObject(name = "COMMENTS", value = CommentSwaggerResponseExample.COMMENTS)
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "400",
                description = "요청값 오류",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "INVALID_INPUT_POST_ID",
                            value = CommentSwaggerErrorExample.Common.INVALID_INPUT_POST_ID
                        ),
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "대상 게시글을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "TARGET_POST", value = CommentSwaggerErrorExample.NotFound.TARGET_POST)
                    ]
                )]
            ),
        ]
    )
    fun getComments(
        @Parameter(description = "댓글을 조회할 게시글 ID", example = "1") postId: PostId,
        @Parameter(description = "현재 로그인한 사용자 ID", hidden = true) writerId: CurrentUserId
    ): ResponseEntity<CommentsResponse>

    @Operation(
        summary = "내가 좋아요한 댓글 조회",
        description = "현재 로그인한 사용자가 LIKE 반응을 누른 댓글 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = LikedCommentsResponse::class),
                )]
            ),
        ]
    )
    fun getLikedComments(
        @ParameterObject
        @PageableDefault(size = 20)
        pageable: Pageable,
        @Parameter(description = "현재 로그인한 사용자 ID", hidden = true) userId: CurrentUserId,
    ): ResponseEntity<LikedCommentsResponse>

    @Operation(
        summary = "내가 작성한 댓글 조회",
        description = "현재 로그인한 사용자가 작성한 댓글 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = CommentSummaryResponse::class),
                )]
            ),
        ]
    )
    fun getCommentsByMe(
        @ParameterObject
        @PageableDefault(size = 20)
        pageable: Pageable,
        @Parameter(description = "현재 로그인한 사용자 ID", hidden = true) userId: CurrentUserId,
    ): ResponseEntity<CommentsResponse>
}

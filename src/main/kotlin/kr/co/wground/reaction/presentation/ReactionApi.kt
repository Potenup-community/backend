package kr.co.wground.reaction.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.reaction.application.dto.CommentReactionStats
import kr.co.wground.reaction.application.dto.PostReactionStats
import kr.co.wground.reaction.docs.SwaggerReactionErrorExample
import kr.co.wground.reaction.docs.SwaggerReactionResponseExample
import kr.co.wground.reaction.presentation.request.CommentReactionStatsBatchRequest
import kr.co.wground.reaction.presentation.request.PostReactionStatsBatchRequest
import kr.co.wground.reaction.presentation.request.ReactionRequest
import org.springframework.http.ResponseEntity

@Tag(name = "Reactions", description = "리액션 API")
interface ReactionApi {

    @Operation(summary = "리액션", description = "필수 요청 바디로 대상 id, 대상 유형, 리액션 유형을 보내어 리액션합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204", description = "생성 성공"
            ),
            ApiResponse(
                responseCode = "400", description = "잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_SUPPORTED_TARGET_TYPE",
                            value = SwaggerReactionErrorExample.BadRequest.NOT_SUPPORTED_TARGET_TYPE
                        ),
                        ExampleObject(
                            name = "NOT_SUPPORTED_REACTION_TYPE",
                            value = SwaggerReactionErrorExample.BadRequest.NOT_SUPPORTED_REACTION_TYPE
                        ),
                        ExampleObject(
                            name = "INVALID_TARGET_TYPE",
                            value = SwaggerReactionErrorExample.BadRequest.INVALID_TARGET_TYPE
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "반응할 대상을 찾지 못함",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "POST_NOT_FOUND",
                            value = SwaggerReactionErrorExample.NotFound.POST_NOT_FOUND
                        ),
                        ExampleObject(
                            name = "COMMENT_NOT_FOUND",
                            value = SwaggerReactionErrorExample.NotFound.COMMENT_NOT_FOUND
                        )
                    ]
                )]
            ),
        ]
    )
    fun react(
        @Valid request: ReactionRequest,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        )
        user: CurrentUserId
    ): ResponseEntity<Unit>

    @Operation(summary = "리액션 취소", description = "필수 요청 바디로 대상 id, 대상 유형, 리액션 유형을 보내어 리액션을 취소합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204", description = "리액션 취소 성공 또는 no-op(이미 취소된 상태)"
            ),
            ApiResponse(
                responseCode = "400", description = "잘못된 요청",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_SUPPORTED_TARGET_TYPE",
                            value = SwaggerReactionErrorExample.BadRequest.NOT_SUPPORTED_TARGET_TYPE
                        ),
                        ExampleObject(
                            name = "NOT_SUPPORTED_REACTION_TYPE",
                            value = SwaggerReactionErrorExample.BadRequest.NOT_SUPPORTED_REACTION_TYPE
                        ),
                        ExampleObject(
                            name = "INVALID_TARGET_TYPE",
                            value = SwaggerReactionErrorExample.BadRequest.INVALID_TARGET_TYPE
                        )
                    ]
                )]
            ),
        ]
    )
    fun unreact(
        @Valid request: ReactionRequest,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) user: CurrentUserId
    ): ResponseEntity<Unit>

    @Operation(summary = "게시글 리액션 조회", description = "경로 변수로 게시글 id 명시하여 해당 게시글의 리액션 유형 별 리액션 수를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "POST_REACTION_STATS_RESPONSE",
                        value = SwaggerReactionResponseExample.POST_REACTION_STATS_RESPONSE
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "유효하지 않은 대상 id 형식",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "INVALID_TARGET_TYPE",
                        value = SwaggerReactionErrorExample.BadRequest.INVALID_TARGET_TYPE
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "404", description = "반응할 게시글을 찾지 못함",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "POST_NOT_FOUND",
                        value = SwaggerReactionErrorExample.NotFound.POST_NOT_FOUND
                    )]
                )]
            ),
        ]
    )
    fun getPostReactionStats(
        @Schema(example = "1")
        @Positive(message = "postId 는 0 또는 음수일 수 없습니다.")
        @NotNull(message = "postId 가 null 입니다.")
        postId: PostId,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) user: CurrentUserId
    ): ResponseEntity<PostReactionStats>

    @Operation(summary = "여러 게시글 리액션 조회", description = "요청 바디로 게시글 id 리스트 명시하여 해당 게시글 목록의 리액션 유형 별 리액션 수를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "POST_REACTION_STATS_IN_BATCH_RESPONSE",
                        value = SwaggerReactionResponseExample.POST_REACTION_STATS_IN_BATCH_RESPONSE
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "너무 작거나 큰 게시글 id 목록",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "INVALID_TARGET_TYPE",
                        value = SwaggerReactionErrorExample.BadRequest.INVALID_POST_ID_LIST_SIZE
                    )]
                )]
            )
        ]
    )
    fun getPostReactionStatsInBatch(
        @Valid request: PostReactionStatsBatchRequest,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) user: CurrentUserId
    ): ResponseEntity<Map<PostId, PostReactionStats>>

    @Operation(summary = "여러 댓글 리액션 조회", description = "요청 바디로 댓글 id 리스트 명시하여 해당 댓글 목록의 리액션 유형 별 리액션 수를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "COMMENT_REACTION_STATS_IN_BATCH_RESPONSE",
                        value = SwaggerReactionResponseExample.COMMENT_REACTION_STATS_IN_BATCH_RESPONSE
                    )]
                )]
            ),
            ApiResponse(
                responseCode = "400", description = "너무 작거나 큰 댓글 id 목록",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [ExampleObject(
                        name = "INVALID_COMMENT_ID_LIST_SIZE",
                        value = SwaggerReactionErrorExample.BadRequest.INVALID_COMMENT_ID_LIST_SIZE
                    )]
                )]
            )
        ]
    )
    fun getCommentReactionStats(
        @Valid request: CommentReactionStatsBatchRequest,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) user: CurrentUserId
    ): ResponseEntity<Map<CommentId, CommentReactionStats>>
}

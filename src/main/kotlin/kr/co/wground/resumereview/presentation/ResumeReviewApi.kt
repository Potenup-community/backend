package kr.co.wground.resumereview.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.headers.Header
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.resumereview.docs.SwaggerResumeReviewErrorExample
import kr.co.wground.resumereview.docs.SwaggerResumeReviewResponseExample
import kr.co.wground.resumereview.presentation.request.CreateResumeReviewRequest
import kr.co.wground.resumereview.presentation.response.ResumeReviewDetailResult
import kr.co.wground.resumereview.presentation.response.ResumeReviewResult
import kr.co.wground.resumereview.presentation.response.ReviewAcceptedResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "ResumeReview", description = "이력서 첨삭 API")
interface ResumeReviewApi {

    @Operation(
        summary = "이력서 첨삭 요청",
        description = "이력서 정보와 JD URL을 기반으로 첨삭을 요청합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "202",
                description = "요청 접수 완료",
                headers = [
                    Header(
                        name = "Location",
                        description = "생성된 이력서 첨삭 리소스 URI",
                        schema = Schema(type = "string", example = "/api/v1/reviews/1")
                    )
                ],
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ReviewAcceptedResult::class),
                    examples = [
                        ExampleObject(
                            name = "ACCEPTED",
                            value = SwaggerResumeReviewResponseExample.REVIEW_ACCEPTED
                        )
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
                            name = "INVALID_INPUT",
                            value = SwaggerResumeReviewErrorExample.INVALID_INPUT
                        )
                    ]
                )]
            )
        ]
    )
    fun reviewRequest(
        @RequestBody request: CreateResumeReviewRequest,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        )
        userId: CurrentUserId
    ): ResponseEntity<ReviewAcceptedResult>


    @Operation(summary = "내 이력서 첨삭 목록 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ResumeReviewResult::class),
                    examples = [
                        ExampleObject(
                            name = "REVIEW_LIST",
                            value = SwaggerResumeReviewResponseExample.REVIEW_LIST
                        )
                    ]
                )]
            )
        ]
    )
    fun getMyReviews(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        )
        userId: CurrentUserId
    ): ResumeReviewResult


    @Operation(summary = "이력서 첨삭 상세 조회")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ResumeReviewDetailResult::class),
                    examples = [
                        ExampleObject(
                            name = "REVIEW_DETAIL",
                            value = SwaggerResumeReviewResponseExample.REVIEW_DETAIL
                        )
                    ]
                )]
            ),
            ApiResponse(
                responseCode = "404",
                description = "자원 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOT_FOUND_REVIEW",
                            value = SwaggerResumeReviewErrorExample.NOT_FOUND_REVIEW
                        )
                    ]
                )]
            )
        ]
    )
    fun getMyReview(
        @Schema(example = "1")
        @PathVariable id: Long,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        )
        userId: CurrentUserId
    ): ResumeReviewDetailResult
}

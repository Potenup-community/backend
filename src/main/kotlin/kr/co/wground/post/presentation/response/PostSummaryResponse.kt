package kr.co.wground.post.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.post.application.dto.PostSummaryDto
import kr.co.wground.post.docs.SwaggerResponseExample
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import org.springframework.data.domain.Slice
import kr.co.wground.post.presentation.response.PostSummaryDetail.PostReactionSummaryDetail
import kr.co.wground.reaction.domain.enums.ReactionType
import java.time.LocalDateTime

@Schema(description = "게시글 서머리 조회 응답 객체", examples = [SwaggerResponseExample.POST_SUMMARY_RESPONSE])
data class PostSummaryResponse(
    val contents: List<PostSummaryDetail>,
    val hasNext: Boolean,
    val nextPage: Int?
)

@Schema(description = "게시글 요약 정보")
data class PostSummaryDetail(
    @field:Schema(example = "1")
    val postId: Long,

    @field:Schema(example = "첫 번째 게시글")
    val title: String,

    @field:Schema(example = "10")
    val writerId: Long,

    @field:Schema(example = "홍길동")
    val writerName: String,

    @field:Schema(
        example = "2026-01-07T14:30:00",
        description = "게시글 작성 시각 (ISO-8601)"
    )
    val wroteAt: LocalDateTime,

    @field:Schema(example = "NOTICE")
    val topic: Topic,

    @field:Schema(
        example = "BY_ADMIN",
        description = "게시글 강조 타입 (없을 수 있음)",
        nullable = true
    )
    val highlightType: HighlightType?,

    @field:Schema(
        example = "3",
        description = "댓글 개수"
    )
    val commentsCount: Int,

    @field:Schema(example = "BE 1기")
    val trackName: String,

    @field:Schema(description = "게시글 리액션 요약 목록")
    val reactions: List<PostReactionSummaryDetail> = emptyList(),
) {
    @Schema(description = "게시글 리액션 요약 정보")
    data class PostReactionSummaryDetail(
        @field:Schema(example = "LIKE")
        val reactionType: ReactionType,

        @field:Schema(example = "5")
        val count: Int,

        @field:Schema(
            example = "true",
            description = "현재 사용자가 해당 리액션을 눌렀는지 여부"
        )
        val reactedByMe: Boolean,
    )
}

fun PostSummaryDto.toResponseDetail() = PostSummaryDetail(
    postId = postId,
    title = title,
    writerId = writerId,
    writerName = writerName,
    wroteAt = wroteAt,
    topic = topic,
    highlightType = highlightType,
    commentsCount = commentsCount,
    trackName = trackName,
    reactions = reactions.map {
        PostReactionSummaryDetail(
            reactionType = it.reactionType,
            count = it.count,
            reactedByMe = it.reactedByMe
        )
    }
)

fun Slice<PostSummaryDto>.toResponse(): PostSummaryResponse {
    return PostSummaryResponse(
        contents = this.content.map { it.toResponseDetail() },
        hasNext = this.hasNext(),
        nextPage = if (this.hasNext()) this.number + 2 else null
    )
}

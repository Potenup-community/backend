package kr.co.wground.post.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId
import kr.co.wground.post.application.dto.PostDetailDto
import kr.co.wground.post.docs.SwaggerResponseExample
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.post.presentation.response.PostDetailResponse.PostReactionDetail
import kr.co.wground.reaction.domain.enums.ReactionType
import java.time.LocalDateTime

@Schema(
    description = "게시글 상세 조회 응답",
    examples = [SwaggerResponseExample.POST_DETAIL_RESPONSE]
)
data class PostDetailResponse(
    val postId: PostId,
    val writerId: WriterId,
    val writerName: String,
    val title: String,
    val content: String,
    val topic: Topic,
    val highlightType: HighlightType?,
    val commentsCount: Int,
    val wroteAt: LocalDateTime,
    val trackName: String,
    val reactions: List<PostReactionDetail> = emptyList(),
) {
    data class PostReactionDetail(
        val reactionType: ReactionType,
        val count: Int,
    )
}

fun PostDetailDto.toResponse() = PostDetailResponse(
    postId = postId,
    writerId = writerId,
    writerName = writerName,
    title = title,
    content = content,
    topic = topic,
    highlightType = highlightType,
    commentsCount = commentsCount,
    wroteAt = wroteAt,
    trackName = trackName,
    reactions = reactions.map { PostReactionDetail(it.reactionType, it.count) }
)

package kr.co.wground.post.presentation.response

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId
import kr.co.wground.post.application.dto.PostDetailDto
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.post.presentation.response.PostDetailResponse.PostReactionDetail
import kr.co.wground.reaction.domain.enums.ReactionType
import java.time.LocalDateTime

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
    reactions = reactions.map { PostReactionDetail(it.reactionType, it.count) }
)

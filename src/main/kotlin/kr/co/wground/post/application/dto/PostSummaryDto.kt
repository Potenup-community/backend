package kr.co.wground.post.application.dto

import kr.co.wground.comment.domain.vo.CommentCount
import kr.co.wground.post.application.dto.PostSummaryDto.PostReactionSummaryDto
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.infra.querydsl.PostReactionQuerydslRepository
import kr.co.wground.reaction.infra.querydsl.PostReactionQuerydslRepository.PostReactionStatsRow
import kr.co.wground.user.domain.User
import java.time.LocalDateTime

data class PostSummaryDto(
    val postId: Long,
    val title: String,
    val writerId: Long,
    val writerName: String,
    val wroteAt: LocalDateTime,
    val topic: Topic,
    val highlightType: HighlightType?,
    val commentsCount: Int,
    val reactions: List<PostReactionSummaryDto> = emptyList(),
) {
    data class PostReactionSummaryDto(
        val reactionType: ReactionType,
        val count: Int,
    )
}

fun PostReactionStatsRow.toDto() = PostReactionSummaryDto(
    reactionType = reactionType,
    count = count.toInt()
)

fun List<Post>.toDtos(
    writers: List<User>,
    commentsCountById: List<CommentCount>,
    postReactionStats: List<PostReactionStatsRow>
): List<PostSummaryDto> {
    val writerNameByIdMap = writers.associate { it.userId to it.name }
    val commentsCountByPostId = commentsCountById.associate { id -> id.postId to id.count }
    val reactionStatsByPostId = postReactionStats.groupBy { it.postId }

    return this.map { post ->
        PostSummaryDto(
            postId = post.id,
            title = post.postBody.title,
            writerId = post.writerId,
            writerName = writerNameByIdMap[post.writerId] ?: "",
            wroteAt = post.createdAt,
            topic = post.topic,
            highlightType = post.postStatus.highlightType,
            commentsCount = commentsCountByPostId[post.id] ?: 0,
            reactions = reactionStatsByPostId[post.id]?.map { it.toDto() } ?: emptyList()
        )
    }
}

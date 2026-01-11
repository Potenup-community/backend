package kr.co.wground.post.application.dto

import java.time.LocalDateTime
import kr.co.wground.comment.domain.vo.CommentCount
import kr.co.wground.global.common.UserId
import kr.co.wground.post.application.dto.PostSummaryDto.PostReactionSummaryDto
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.infra.querydsl.CustomPostReactionRepositoryImpl.PostReactionStatsRow
import kr.co.wground.user.application.operations.constant.UNKNOWN_USER_NAME_TAG
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl

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
        val reactedByMe: Boolean,
    )
}

fun PostReactionStatsRow.toDto() = PostReactionSummaryDto(
    reactionType = reactionType,
    count = count.toInt(),
    reactedByMe = reactedByMe
)

fun Slice<Post>.toDtos(
    writersById: Map<UserId, UserDisplayInfoDto>,
    commentsCountById: List<CommentCount>,
    postReactionStats: List<PostReactionStatsRow>
): Slice<PostSummaryDto> {
    val commentsCountByPostId = commentsCountById.associate { id -> id.postId to id.count }
    val reactionStatsByPostId = postReactionStats.groupBy { it.postId }

    val dtoContent = this.content.map { post ->
        PostSummaryDto(
            postId = post.id,
            title = post.postBody.title,
            writerId = post.writerId,
            writerName = writersById[post.writerId]?.name ?: UNKNOWN_USER_NAME_TAG,
            wroteAt = post.createdAt,
            topic = post.topic,
            highlightType = post.postStatus.highlightType,
            commentsCount = commentsCountByPostId[post.id] ?: 0,
            reactions = reactionStatsByPostId[post.id]?.map { it.toDto() } ?: emptyList()
        )
    }

    return SliceImpl(
        dtoContent,
        this.pageable,
        this.hasNext()
    )
}

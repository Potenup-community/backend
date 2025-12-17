package kr.co.wground.post.application.dto

import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.user.domain.User
import java.time.LocalDateTime

class PostSummaryDto(
    val postId: Long,
    val title: String,
    val writerId: Long,
    val writerName: String,
    val wroteAt: LocalDateTime,
    val topic: Topic,
    val highlightType: HighlightType?,
    val commentsCount: Int,
)

fun List<Post>.toDtos(writers: List<User>): List<PostSummaryDto> {
    val writerNameByIdMap = writers.associate { it.userId to it.name }

    return this.map { post ->
        PostSummaryDto(
            postId = post.id,
            title = post.postBody.title,
            writerId = post.writerId,
            writerName = writerNameByIdMap[post.writerId] ?: "",
            wroteAt = post.createdAt,
            topic = post.topic,
            highlightType = post.postStatus.highlightType,
            commentsCount = 0
        )
    }
}

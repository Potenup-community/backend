package kr.co.wground.comment.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import kr.co.wground.comment.application.dto.MyCommentSummaryDto
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.reaction.application.dto.CommentReactionStats
import org.springframework.data.domain.Slice

data class MyCommentsResponse(
    val contents: List<MyCommentItemResponse>,
    val hasNext: Boolean,
    val nextPage: Int?,
)

data class MyCommentItemResponse(
    @field:Schema(description = "댓글 ID")
    val commentId: CommentId,
    @field:Schema(description = "게시글 ID")
    val postId: PostId,
    @field:Schema(description = "댓글 내용")
    val content: String,
    @field:Schema(description = "댓글 작성자 정보")
    val author: CommentAuthorResponse,
    @field:Schema(description = "댓글 작성 시간")
    val createdAt: LocalDateTime,
    @field:Schema(description = "댓글 반응 통계")
    val commentReactionStats: CommentReactionStats,
    @field:Schema(description = "댓글 삭제 여부")
    val isDeleted: Boolean,
) {
    companion object {
        fun from(dto: MyCommentSummaryDto): MyCommentItemResponse {
            return MyCommentItemResponse(
                commentId = dto.commentId,
                postId = dto.postId,
                content = dto.content,
                author = CommentAuthorResponse(
                    userId = dto.authorId,
                    name = dto.authorName,
                    trackName = dto.trackName,
                    profileImageUrl = dto.authorProfileImageUrl,
                ),
                createdAt = dto.createdAt,
                commentReactionStats = dto.commentReactionStats,
                isDeleted = dto.isDeleted,
            )
        }
    }
}

fun Slice<MyCommentSummaryDto>.toResponse(): MyCommentsResponse {
    return MyCommentsResponse(
        contents = this.content.map { MyCommentItemResponse.from(it) },
        hasNext = this.hasNext(),
        nextPage = if (this.hasNext()) this.number + 2 else null,
    )
}

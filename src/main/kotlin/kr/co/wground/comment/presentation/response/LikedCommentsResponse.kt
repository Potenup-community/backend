package kr.co.wground.comment.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime
import kr.co.wground.comment.application.dto.LikedCommentSummaryDto
import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.reaction.application.dto.CommentReactionStats
import org.springframework.data.domain.Slice

@Schema(description = "내가 좋아요한 댓글 목록 응답 객체")
data class LikedCommentsResponse(
    val contents: List<LikedCommentSummaryResponse>,
    val hasNext: Boolean,
    val nextPage: Int?,
)

data class LikedCommentSummaryResponse(
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
    @field:Schema(description = "좋아요 누른 시간")
    val likedAt: LocalDateTime,
    @field:Schema(description = "댓글 반응 통계")
    val commentReactionStats: CommentReactionStats,
    @field:Schema(description = "댓글 삭제 여부")
    val isDeleted: Boolean,
)

fun Slice<LikedCommentSummaryDto>.toResponse(): LikedCommentsResponse {
    return LikedCommentsResponse(
        contents = this.content.map {
            LikedCommentSummaryResponse(
                commentId = it.commentId,
                postId = it.postId,
                content = it.content,
                author = CommentAuthorResponse(
                    userId = it.authorId,
                    name = it.authorName,
                    trackName = it.trackName,
                    profileImageUrl = it.authorProfileImageUrl,
                ),
                createdAt = it.createdAt,
                likedAt = it.likedAt,
                commentReactionStats = it.commentReactionStats,
                isDeleted = it.isDeleted,
            )
        },
        hasNext = this.hasNext(),
        nextPage = if (this.hasNext()) this.number + 2 else null,
    )
}


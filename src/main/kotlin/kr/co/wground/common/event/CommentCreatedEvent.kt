package kr.co.wground.common.event

import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId

data class CommentCreatedEvent(
    val postId: PostId,
    val postWriterId: WriterId,
    val commentId: CommentId,
    val commentWriterId: WriterId,
    val parentCommentId: CommentId?,
    val parentCommentWriterId: WriterId?,
)

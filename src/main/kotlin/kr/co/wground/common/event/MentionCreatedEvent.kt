package kr.co.wground.common.event

import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId

data class MentionCreatedEvent(
    val postId: PostId,
    val commentId: CommentId,
    val mentionerId: UserId, // 멘션"한" 사용자
    val mentionUserIds: List<UserId>, // 멘션"된" 사용자
)

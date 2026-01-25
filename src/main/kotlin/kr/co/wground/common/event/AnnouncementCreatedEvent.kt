package kr.co.wground.common.event

import kr.co.wground.global.common.PostId

data class AnnouncementCreatedEvent(
    val postId: PostId,
    val title: String,
)

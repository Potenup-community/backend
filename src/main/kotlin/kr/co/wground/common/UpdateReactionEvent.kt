package kr.co.wground.common

import kr.co.wground.global.common.PostId
import java.util.UUID

data class UpdateReactionEvent(
    val eventId: UUID,
    val postId: PostId,
    val delta: Delta,
) {

}

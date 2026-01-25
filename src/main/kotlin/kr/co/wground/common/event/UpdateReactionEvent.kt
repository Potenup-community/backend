package kr.co.wground.common.event

import kr.co.wground.global.common.PostId
import java.util.UUID
import kr.co.wground.common.Delta

data class UpdateReactionEvent(
    val eventId: UUID,
    val postId: PostId,
    val delta: Delta,
) {
}

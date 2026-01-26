package kr.co.wground.common.event

import kr.co.wground.global.common.OwnerId
import kr.co.wground.global.common.PostId
import java.util.UUID

class SyncDraftImagesToPostEvent(
    val postId: PostId,
    val ownerId: OwnerId,
    val draftId: UUID,
    val markdown: String,
) {
}

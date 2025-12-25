package kr.co.wground.image.infra.dto

import kr.co.wground.global.common.OwnerId
import kr.co.wground.global.common.PostId
import java.util.UUID

data class MarkOrphanByDraftDto(
    val postId: PostId,
    val ownerId: OwnerId,
    val draftId: UUID,
    val paths: Set<String>,
) {
}

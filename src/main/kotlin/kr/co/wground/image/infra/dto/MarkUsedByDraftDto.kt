package kr.co.wground.image.infra.dto

import kr.co.wground.global.common.OwnerId
import kr.co.wground.global.common.PostId
import java.util.UUID

data class MarkUsedByDraftDto(
    val ownerId: OwnerId,
    val draftId: UUID,
    val postId: PostId,
    val paths: Set<String>,
) {
}

package kr.co.wground.post.application.dto

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.WriterId
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import java.util.UUID

data class PostUpdateDto(
    val postId: PostId,
    val draftId: UUID?,
    val title: String? = null,
    val content: String? = null,
    val writerId: WriterId,
    val topic: Topic? = null,
    val highlightType: HighlightType? = null,
) {

}

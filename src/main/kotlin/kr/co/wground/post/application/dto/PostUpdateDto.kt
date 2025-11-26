package kr.co.wground.post.application.dto

import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic

data class PostUpdateDto(
    val id: PostId,
    val title: String? = null,
    val content: String? = null,
    val writerId: UserId,
    val topic: Topic? = null,
    val highlightType: HighlightType? = null,
) {

}

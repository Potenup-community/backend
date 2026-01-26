package kr.co.wground.post.infra.predicate

import kr.co.wground.global.common.UserId
import kr.co.wground.post.domain.enums.Topic
import org.springframework.data.domain.Pageable

data class GetPostSummaryPredicate(
    val pageable: Pageable,
    val topic: Topic? = null,
    val userId: UserId? = null,
) {

}

package kr.co.wground.post.infra.predicate

import kr.co.wground.post.domain.enums.Topic
import org.springframework.data.domain.Pageable

data class GetPostSummaryPredicate(
    val pageable: Pageable,
    val topic: Topic?
) {

}

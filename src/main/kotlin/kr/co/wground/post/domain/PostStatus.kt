package kr.co.wground.post.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.post.domain.enums.HighlightType

@Entity
class PostStatus(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val viewCount: Int = 0,
    val commentCount: Int = 0,
    val likeCount: Int = 0,
    highlightType: HighlightType? = null,
    val isDeleted: Boolean = false,
) {
    @Enumerated(EnumType.STRING)
    var highlightType: HighlightType? = highlightType
        protected set
    fun highlight(type: HighlightType?) {
        highlightType = type
    }
}

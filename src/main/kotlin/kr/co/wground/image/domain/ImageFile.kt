package kr.co.wground.image.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.global.common.OwnerId
import kr.co.wground.global.common.PostId
import kr.co.wground.image.domain.enums.ImageStatus
import kr.co.wground.image.domain.enums.ImageStatus.DELETED
import kr.co.wground.image.domain.enums.ImageStatus.TEMP
import kr.co.wground.image.domain.enums.ImageStatus.USED
import java.time.LocalDateTime
import java.util.UUID

@Entity
class ImageFile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val ownerId: OwnerId,
    postId: PostId? = null,
    val draftId: UUID,
    status: ImageStatus = TEMP,
    val relativePath: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Enumerated(EnumType.STRING)
    var status: ImageStatus = status
        protected set

    var postId: PostId? = postId
        protected set

    companion object {
        fun create(ownerId: OwnerId, draftId: UUID, relativePath: String) =
            ImageFile(ownerId = ownerId, draftId = draftId, relativePath = relativePath)
    }

    fun markUsed() {
        this.status = USED
    }

    fun markOrphan() {
        this.status = DELETED
    }

    fun fillPostId(postId: PostId) {
        this.postId = postId
    }
}

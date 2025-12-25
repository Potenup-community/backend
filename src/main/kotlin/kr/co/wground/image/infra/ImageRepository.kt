package kr.co.wground.image.infra

import kr.co.wground.global.common.OwnerId
import kr.co.wground.image.domain.ImageFile
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ImageRepository: JpaRepository<ImageFile, Long>, CustomImageRepository {
    fun findAllByDraftIdAndOwnerId(draftId: UUID, ownerId: OwnerId): List<ImageFile>
}

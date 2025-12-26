package kr.co.wground.image.infra

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.global.common.PostId
import kr.co.wground.image.domain.ImageFile
import kr.co.wground.image.domain.QImageFile.imageFile
import kr.co.wground.image.domain.enums.ImageStatus
import kr.co.wground.image.infra.dto.MarkOrphanByDraftDto
import kr.co.wground.image.infra.dto.MarkUsedByDraftDto
import java.time.LocalDateTime

class CustomImageRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
) : CustomImageRepository {

    override fun findTempOlderThan(cutoff: LocalDateTime): List<ImageFile> {
        return jpaQueryFactory
            .selectFrom(imageFile)
            .where(
                imageFile.createdAt.lt(cutoff),
                imageFile.status.`in`(ImageStatus.ORPHAN, ImageStatus.TEMP),
            ).fetch()
    }

    override fun markUsedAndFillPostIdByDraft(dto: MarkUsedByDraftDto) {
        jpaQueryFactory
            .update(imageFile)
            .set(imageFile.postId, dto.postId)
            .set(imageFile.status, ImageStatus.USED)
            .where(
                imageFile.ownerId.eq(dto.ownerId),
                imageFile.draftId.eq(dto.draftId),
                imageFile.relativePath.`in`(dto.paths),
            )
            .execute()
    }

    override fun markOrphanByDraftNotInPaths(dto: MarkOrphanByDraftDto) {
        jpaQueryFactory
            .update(imageFile)
            .set(imageFile.status, ImageStatus.ORPHAN)
            .where(
                imageFile.draftId.eq(dto.draftId)
                    .or(eqIfNotNull(dto.postId)),
                imageFile.ownerId.eq(dto.ownerId),
                imageFile.relativePath.notIn(dto.paths),
            )
            .execute()
    }

    private fun eqIfNotNull(postId: PostId?) = postId?.let { imageFile.postId.eq(it) }
}

package kr.co.wground.image.infra

import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.image.domain.ImageFile
import kr.co.wground.image.domain.QImageFile.imageFile
import kr.co.wground.image.domain.enums.ImageStatus.*
import java.time.LocalDateTime

class CustomImageRepositoryImpl(
    private val jpaQueryFactory: JPAQueryFactory
): CustomImageRepository {

    override fun findTempOlderThan(cutoff: LocalDateTime): List<ImageFile> {
        return jpaQueryFactory
            .selectFrom(imageFile)
            .where(
                imageFile.createdAt.lt(cutoff),
                imageFile.status.`in`(DELETED, TEMP),
            ).fetch()
    }
}

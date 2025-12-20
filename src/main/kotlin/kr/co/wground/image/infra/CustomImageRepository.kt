package kr.co.wground.image.infra

import kr.co.wground.image.domain.ImageFile
import java.time.LocalDateTime

interface CustomImageRepository {
    fun findTempOlderThan(cutoff: LocalDateTime): List<ImageFile>
}

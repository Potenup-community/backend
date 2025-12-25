package kr.co.wground.image.infra

import kr.co.wground.image.domain.ImageFile
import kr.co.wground.image.infra.dto.MarkOrphanByDraftDto
import kr.co.wground.image.infra.dto.MarkUsedByDraftDto
import java.time.LocalDateTime

interface CustomImageRepository {
    fun findTempOlderThan(cutoff: LocalDateTime): List<ImageFile>
    fun markUsedAndFillPostIdByDraft(dto: MarkUsedByDraftDto)
    fun markOrphanByDraftNotInPaths(dto: MarkOrphanByDraftDto)
}

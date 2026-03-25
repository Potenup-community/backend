package kr.co.wground.point.presentation.response

import kr.co.wground.point.application.query.dto.PointHistorySummaryDto
import kr.co.wground.point.domain.PointReferenceType
import kr.co.wground.point.domain.PointType
import java.time.LocalDateTime
import org.springframework.data.domain.Slice

data class PointHistoriesResponse(
    val histories: List<PointHistoryResponse>,
    val hasNext: Boolean,
)

data class PointHistoryResponse(
    val id: Long,
    val amount: Long,
    val type: PointType,
    val description: String,
    val refType: PointReferenceType,
    val refId: Long,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(dto: PointHistorySummaryDto): PointHistoryResponse {
            return PointHistoryResponse(
                id = dto.id,
                amount = dto.amount,
                type = dto.type,
                description = dto.description,
                refType = dto.refType,
                refId = dto.refId,
                createdAt = dto.createdAt
            )
        }
    }
}

fun Slice<PointHistorySummaryDto>.toResponse(): PointHistoriesResponse =
    PointHistoriesResponse(
        histories = content.map(PointHistoryResponse::from),
        hasNext = hasNext()
    )
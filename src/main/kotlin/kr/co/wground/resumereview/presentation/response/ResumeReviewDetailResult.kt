package kr.co.wground.resumereview.presentation.response

import kr.co.wground.resumereview.application.query.dto.ResumeReviewDetailResultDto
import kr.co.wground.resumereview.domain.ResumeReviewStatus
import kr.co.wground.resumereview.domain.vo.ResumeSection
import java.time.LocalDateTime

data class ResumeReviewDetailResult(
    val resumeReviewId: Long,
    val resumeReviewTitle: String,
    val resumeSections: ResumeSection,
    val resultJason: String?,
    val completedAt: LocalDateTime?,
    val status: ResumeReviewStatus,
    val createdAt: LocalDateTime,
)

fun ResumeReviewDetailResultDto.toResponse() =
    ResumeReviewDetailResult(
        resumeReviewId = resumeReviewId,
        resumeReviewTitle = resumeReviewTitle,
        resumeSections = resumeSections,
        resultJason = resultJason,
        completedAt = completedAt,
        status = status,
        createdAt = createdAt
    )

package kr.co.wground.resumereview.application.query.dto

import kr.co.wground.resumereview.domain.ResumeReview
import kr.co.wground.resumereview.domain.ResumeReviewStatus
import kr.co.wground.resumereview.domain.vo.ResumeSection
import java.time.LocalDateTime

data class ResumeReviewDetailResultDto(
    val resumeReviewId: Long,
    val resumeReviewTitle: String,
    val resumeSections: ResumeSection,
    val resultJason: String?,
    val completedAt: LocalDateTime?,
    val status: ResumeReviewStatus,
    val createdAt: LocalDateTime,
)

fun ResumeReview.toDto(): ResumeReviewDetailResultDto =
    ResumeReviewDetailResultDto(
        resumeReviewId = id,
        resumeReviewTitle = resumeReviewTitle,
        resumeSections = resumeSections,
        resultJason = resultJson,
        completedAt = completedAt,
        status = status,
        createdAt = createdAt
    )

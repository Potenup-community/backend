package kr.co.wground.resumereview.application.query.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.resumereview.domain.ResumeReview
import kr.co.wground.resumereview.domain.ResumeReviewStatus

data class ResumeReviewResultDto(
    val resumeReviewId: Long,
    val userId: UserId,
    val resumeReviewTitle: String,
    val status: ResumeReviewStatus,
)

fun List<ResumeReview>.toDto(): List<ResumeReviewResultDto> =
    map {
        ResumeReviewResultDto(
            resumeReviewId = it.id,
            userId = it.userId,
            resumeReviewTitle = it.resumeReviewTitle,
            status = it.status
        )
    }

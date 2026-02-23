package kr.co.wground.resumereview.application.command.dto

import kr.co.wground.resumereview.domain.ResumeReviewStatus

data class ReviewAcceptedResultDto(
    val resumeReviewId: Long,
    val status: ResumeReviewStatus,
)

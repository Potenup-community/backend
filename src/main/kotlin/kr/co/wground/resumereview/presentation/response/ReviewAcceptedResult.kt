package kr.co.wground.resumereview.presentation.response

import kr.co.wground.resumereview.application.command.dto.ReviewAcceptedResultDto
import kr.co.wground.resumereview.domain.ResumeReviewStatus

data class ReviewAcceptedResult(
    val resumeReviewId: Long,
    val status: ResumeReviewStatus,
)

fun ReviewAcceptedResultDto.toResponse() = ReviewAcceptedResult(resumeReviewId, status)

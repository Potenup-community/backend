package kr.co.wground.resumereview.presentation.response

import kr.co.wground.global.common.UserId
import kr.co.wground.resumereview.application.query.dto.ResumeReviewResultDto
import kr.co.wground.resumereview.domain.ResumeReviewStatus
import kr.co.wground.resumereview.presentation.response.ResumeReviewResult.ResumeReviewResultContent

data class ResumeReviewResult(
    val contents: List<ResumeReviewResultContent>
) {
    data class ResumeReviewResultContent(
        val resumeReviewId: Long,
        val userId: UserId,
        val resumeReviewTitle: String,
        val status: ResumeReviewStatus,
    )
}

fun List<ResumeReviewResultDto>.toResponse(): ResumeReviewResult {
    return ResumeReviewResult(map { it.toContent() })
}

fun ResumeReviewResultDto.toContent() = ResumeReviewResultContent(
    resumeReviewId = resumeReviewId,
    userId = userId,
    resumeReviewTitle = resumeReviewTitle,
    status = status
)



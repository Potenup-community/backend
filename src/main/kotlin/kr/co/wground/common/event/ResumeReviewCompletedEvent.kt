package kr.co.wground.common.event

import kr.co.wground.global.common.UserId

data class ResumeReviewCompletedEvent(
    val userId: UserId,
    val resumeReviewId: Long,
) {
}

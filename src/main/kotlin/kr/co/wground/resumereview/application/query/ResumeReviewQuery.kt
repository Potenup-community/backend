package kr.co.wground.resumereview.application.query

import kr.co.wground.global.common.UserId
import kr.co.wground.resumereview.application.query.dto.ResumeReviewDetailResultDto
import kr.co.wground.resumereview.application.query.dto.ResumeReviewResultDto

interface ResumeReviewQuery {
    fun getMyReviews(userId: Long): List<ResumeReviewResultDto>
    fun getMyReview(id: Long, userId: UserId): ResumeReviewDetailResultDto
}

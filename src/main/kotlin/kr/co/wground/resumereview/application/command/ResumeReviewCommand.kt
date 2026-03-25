package kr.co.wground.resumereview.application.command

import kr.co.wground.resumereview.application.command.dto.CreateResumeReviewDto
import kr.co.wground.resumereview.application.command.dto.ReviewAcceptedResultDto

interface ResumeReviewCommand {
    fun review(dto: CreateResumeReviewDto): ReviewAcceptedResultDto
}

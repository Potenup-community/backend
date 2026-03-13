package kr.co.wground.resumereview.presentation.request

import kr.co.wground.global.common.UserId
import kr.co.wground.resumereview.application.command.dto.CreateResumeReviewDto

data class CreateResumeReviewRequest(
    val resumeReviewTitle: String,
    val jdUrl: String,
    val summary: String,
    val skills: String,
    val experience: String,
    val education: String,
    val projects: String,
    val cert: String,
) {
    fun toDto(userId: UserId) = CreateResumeReviewDto(
        userId = userId,
        resumeReviewTitle = resumeReviewTitle,
        jdUrl = jdUrl,
        summary = summary,
        skills = skills,
        experience = experience,
        education = education,
        projects = projects,
        cert = cert
    )
}

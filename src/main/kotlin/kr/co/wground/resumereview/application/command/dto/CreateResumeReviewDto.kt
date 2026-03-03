package kr.co.wground.resumereview.application.command.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.resumereview.domain.ResumeReview
import kr.co.wground.resumereview.domain.vo.ResumeSection

data class CreateResumeReviewDto(
    val userId: UserId,
    val resumeReviewTitle: String,
    val jdUrl: String,
    val summary: String,
    val skills: String,
    val experience: String,
    val education: String,
    val projects: String,
    val cert: String,
)

fun CreateResumeReviewDto.toDomain(hashedRequest: String): ResumeReview {
    val resumeSections = ResumeSection.of(
        summary = summary,
        skills = skills,
        experience = experience,
        education = education,
        projects = projects,
        cert = cert
    )

    return ResumeReview.create(
        userId = userId,
        resumeReviewTitle = resumeReviewTitle,
        jdUrl = jdUrl,
        hash = hashedRequest,
        resumeSections = resumeSections
    )
}

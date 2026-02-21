package kr.co.wground.common.event

import kr.co.wground.resumereview.domain.vo.ResumeSection
import java.util.UUID

data class ReviewRequestedEvent(
    val eventId: UUID = UUID.randomUUID(),
    val resumeReviewId: Long,
    val userId: Long,
    val title: String,
    val sections: Map<SectionType, String>,
    val url: String,
) {
    companion object {
        fun of(
            resumeReviewId: Long,
            userId: Long,
            title: String,
            jdUrl: String,
            section: ResumeSection,
        ) = ReviewRequestedEvent(
            resumeReviewId = resumeReviewId,
            userId = userId,
            title = title,
            url = jdUrl,
            sections = section.toEventSections(),
        )
    }
}

private fun ResumeSection.toEventSections(): Map<SectionType, String> =
    linkedMapOf(
        SectionType.SUMMARY to summary,
        SectionType.SKILLS to skills,
        SectionType.EXPERIENCE to experience,
        SectionType.EDUCATION to education,
        SectionType.PROJECTS to projects,
        SectionType.CERTS to cert,
    ).filterValues(String::isNotBlank)

enum class SectionType {
    SUMMARY,
    SKILLS,
    EXPERIENCE,
    PROJECTS,
    EDUCATION,
    CERTS,
}

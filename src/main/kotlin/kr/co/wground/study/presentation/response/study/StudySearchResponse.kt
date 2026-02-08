package kr.co.wground.study.presentation.response.study

import java.time.LocalDateTime
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.enums.StudyStatus

@Deprecated(message = "사용 안 하는듯?")
data class StudySearchResponse(
    val id: Long,
    val name: String,
    val description: String,
    val capacity: Int,
    val currentMemberCount: Int,
    val status: StudyStatus,
    val tags: List<String>,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(study: Study): StudySearchResponse {
            return StudySearchResponse(
                id = study.id,
                name = study.name,
                description = study.description,
                capacity = study.capacity,
                currentMemberCount = study.recruitments.size,
                status = study.status,
                tags = study.studyTags.map { it.tag.name },
                createdAt = study.createdAt
            )
        }
    }
}

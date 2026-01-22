package kr.co.wground.study.presentation.response.study

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.constant.BudgetType
import kr.co.wground.study.domain.constant.StudyStatus
import java.time.LocalDateTime

data class StudyDetailResponse(
    val id: Long,
    val scheduleId: Long,
    val leaderId: UserId,
    val name: String,
    val description: String,
    val capacity: Int,
    val currentMemberCount: Int,
    val status: StudyStatus,
    val budget: BudgetType,
    val chatUrl: String?,
    val refUrl: String?,
    val tags: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isRecruitmentClosed: Boolean
) {
    companion object {
        fun from(study: Study, canViewChatUrl: Boolean): StudyDetailResponse {
            return StudyDetailResponse(
                id = study.id,
                scheduleId = study.schedule.id,
                leaderId = study.leaderId,
                name = study.name,
                description = study.description,
                capacity = study.capacity,
                currentMemberCount = study.currentMemberCount,
                status = study.status,
                budget = study.budget,
                chatUrl = if (canViewChatUrl) study.externalChatUrl else null,
                refUrl = study.referenceUrl,
                tags = study.studyTags.map { it.tag.name },
                createdAt = study.createdAt,
                updatedAt = study.updatedAt,
                isRecruitmentClosed = study.schedule.isRecruitmentClosed()
            )
        }
    }
}
package kr.co.wground.study.presentation.response.study

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.constant.BudgetType
import kr.co.wground.study.domain.constant.StudyStatus
import java.time.LocalDateTime
import kr.co.wground.study.domain.StudySchedule

data class StudyDetailResponse(
    val id: Long,
    val scheduleId: Long,
    val scheduleName: String,
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
    val isRecruitmentClosed: Boolean,
    val isLeader: Boolean,
) {
    companion object {
        fun of(study: Study, canViewChatUrl: Boolean, schedule: StudySchedule, userId: UserId?): StudyDetailResponse {
            return StudyDetailResponse(
                id = study.id,
                scheduleId = schedule.id,
                scheduleName = schedule.months.month,
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
                isRecruitmentClosed = schedule.isRecruitmentClosed(),
                isLeader = isStudyLeader(study.leaderId, userId)
            )
        }

        fun isStudyLeader(leaderId: UserId, userId: UserId?): Boolean {
            if (userId == null) return false
            return userId == leaderId
        }
    }
}
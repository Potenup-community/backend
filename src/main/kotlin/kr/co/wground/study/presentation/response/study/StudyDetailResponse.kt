package kr.co.wground.study.presentation.response.study

import kr.co.wground.global.common.UserId
import kr.co.wground.study.application.dto.ParticipantInfo
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.enums.BudgetType
import kr.co.wground.study.domain.enums.StudyStatus
import kr.co.wground.study_schedule.application.dto.ScheduleDto
import java.time.LocalDateTime
import kr.co.wground.study_schedule.domain.StudySchedule

data class StudyDetailResponse(
    val id: Long,
    val name: String,
    val description: String,
    val week1Plan: String,
    val week2Plan: String,
    val week3Plan: String,
    val week4Plan: String,
    val capacity: Int,
    val currentMemberCount: Int,
    val status: StudyStatus,
    val budget: BudgetType,
    val budgetExplain: String,
    val chatUrl: String?,
    val refUrl: String?,
    val tags: List<String>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val isRecruitmentClosed: Boolean,
    val isLeader: Boolean,
    val isParticipant: Boolean,  // leader 인 경우에도 true
    val participants: List<ParticipantInfo>,  // 스터디 장 포함

    val schedule: ScheduleDto,
    val leader: ParticipantInfo,
) {
    companion object {
        fun of(
            study: Study,
            userId: UserId,
            schedule: StudySchedule,
            participants: List<ParticipantInfo>
        ): StudyDetailResponse {
            return StudyDetailResponse(
                id = study.id,
                name = study.name,
                description = study.description,
                week1Plan = study.weeklyPlans.week1Plan,
                week2Plan = study.weeklyPlans.week2Plan,
                week3Plan = study.weeklyPlans.week3Plan,
                week4Plan = study.weeklyPlans.week4Plan,
                capacity = study.capacity,
                currentMemberCount = study.recruitments.size,
                status = study.status,
                budget = study.budget,
                budgetExplain = study.budgetExplain,
                chatUrl = if (study.recruitments.any{ it.userId == userId }) study.externalChatUrl else null,
                refUrl = study.referenceUrl,
                tags = study.studyTags.map { it.tag.name },
                createdAt = study.createdAt,
                updatedAt = study.updatedAt,
                isRecruitmentClosed = schedule.isRecruitmentClosed(),
                isLeader = study.leaderId == userId,
                isParticipant = study.recruitments.any { it.userId == userId },
                participants = participants,
                schedule = ScheduleDto.from(schedule),
                leader = participants.first { it.id == study.leaderId },
            )
        }
    }
}

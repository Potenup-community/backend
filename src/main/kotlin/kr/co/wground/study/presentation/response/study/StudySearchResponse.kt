package kr.co.wground.study.presentation.response.study

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.Study
import kr.co.wground.study.domain.enums.StudyStatus
import java.time.LocalDateTime
import kr.co.wground.study.application.dto.ParticipantInfo

data class StudySearchResponse(
    val id: Long,
    val name: String,
    val description: String,
    val capacity: Int,
    val currentMemberCount: Int,
    val status: StudyStatus,
    val chatUrl: String?,
    val tags: List<String>,

    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,

    val isLeader: Boolean,
    val isParticipant: Boolean,
    val leader: ParticipantInfo,
) {
    companion object {
        fun of(
            study: Study,
            userId: UserId?,
            leaderInfo: ParticipantInfo,
        ): StudySearchResponse {
            return StudySearchResponse(
                id = study.id,
                name = study.name,
                description = study.description,
                capacity = study.capacity,
                currentMemberCount = study.recruitments.size,
                status = study.status,
                chatUrl = if (study.recruitments.any{ it.userId == userId }) study.externalChatUrl else null,
                tags = study.studyTags.map { it.tag.name },
                createdAt = study.createdAt,
                updatedAt = study.updatedAt,
                isLeader = study.leaderId == userId,
                isParticipant = study.recruitments.any { it.userId == userId },
                leader = leaderInfo,
            )
        }
    }
}
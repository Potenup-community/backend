package kr.co.wground.study.presentation.response.recruit

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.shop.application.dto.EquippedItem
import kr.co.wground.study.domain.StudyRecruitment

data class StudyRecruitmentResponse(
    val id: Long,
    val studyId: Long,
    val studyName: String,
    val trackName: String,
    val userId: UserId,
    val userName: String,
    val createdAt: LocalDateTime,
    val items: List<EquippedItem>
) {
    companion object {
        fun of(recruitment: StudyRecruitment, userName: String, trackName: String, items: List<EquippedItem>): StudyRecruitmentResponse {
            return StudyRecruitmentResponse(
                id = recruitment.id,
                studyId = recruitment.study.id,
                studyName = recruitment.study.name,
                trackName = trackName,
                userId = recruitment.userId,
                userName = userName,
                createdAt = recruitment.createdAt,
                items = items
            )
        }
    }
}

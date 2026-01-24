package kr.co.wground.study.presentation.response.recruit

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.StudyRecruitment
import kr.co.wground.study.domain.constant.RecruitStatus

data class StudyRecruitmentResponse(
    val id: Long,
    val studyId: Long,
    val studyName: String,
    val trackName: String,
    val userId: UserId,
    val userName: String,
    val appeal: String,
    val status: RecruitStatus,
    val createdAt: LocalDateTime,
    val approvedAt: LocalDateTime?
) {
    companion object {
        fun of(recruitment: StudyRecruitment, userName: String, trackName: String): StudyRecruitmentResponse {
            return StudyRecruitmentResponse(
                id = recruitment.id,
                studyId = recruitment.study.id,
                studyName = recruitment.study.name,
                trackName = trackName,
                userId = recruitment.userId,
                userName = userName,
                appeal = recruitment.appeal,
                status = recruitment.recruitStatus,
                createdAt = recruitment.createdAt,
                approvedAt = recruitment.approvedAt
            )
        }
    }
}

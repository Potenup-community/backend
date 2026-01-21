package kr.co.wground.study.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.constant.RecruitStatus
import java.time.LocalDateTime

@Entity
class StudyRecruitment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val userId: UserId,
    val studyId: Long,
    val appeal: String,
    val recruitStatus: RecruitStatus,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
}
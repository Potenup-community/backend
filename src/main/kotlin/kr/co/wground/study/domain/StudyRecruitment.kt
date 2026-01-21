package kr.co.wground.study.domain

import jakarta.persistence.Column
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
    @Column(nullable = false)
    val userId: UserId,
    @Column(nullable = false)
    val studyId: Long,
    @Column(nullable = false)
    val appeal: String,
    recruitStatus: RecruitStatus,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @Column(nullable = false)
    var recruitStatus: RecruitStatus = recruitStatus
        protected set

    @Column(nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    //TODO 내일 여기까지  만들면 도메인 구축은 끝
}
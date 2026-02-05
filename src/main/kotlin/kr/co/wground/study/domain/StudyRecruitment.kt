package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import kr.co.wground.global.common.UserId
import java.time.LocalDateTime

@Entity
class StudyRecruitment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(nullable = false)
    val userId: UserId,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    val study: Study,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {

    companion object {
        fun apply(userId: UserId, study: Study): StudyRecruitment {
            return StudyRecruitment(
                userId = userId,
                study = study,
            )
        }

        fun createByLeader(userId: UserId, study: Study): StudyRecruitment {
            return StudyRecruitment(
                userId = userId,
                study = study,
            )
        }
    }
}

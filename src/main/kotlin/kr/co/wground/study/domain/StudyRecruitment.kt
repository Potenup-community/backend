package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.constant.RecruitStatus
import kr.co.wground.study.domain.constant.StudyStatus
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
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
    appeal: String,
    recruitStatus: RecruitStatus,
    approvedAt: LocalDateTime? = null,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @Column(nullable = false, length = MAX_APPEAL_LENGTH)
    var appeal: String = appeal
        protected set

    @Column(nullable = false)
    var recruitStatus: RecruitStatus = recruitStatus
        protected set

    @Column(nullable = true)
    var approvedAt: LocalDateTime? = approvedAt
        protected set

    @Column(nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    companion object {
        const val MAX_APPEAL_LENGTH = 200
        const val LEADER_DEFAULT_APPEAL = "스터디장 자동 참여"

        fun apply(userId: UserId, appeal: String, study: Study): StudyRecruitment {
            val trimmedAppeal = appeal.trim()
            validateAppealLength(trimmedAppeal)

            return StudyRecruitment(
                userId = userId,
                appeal = trimmedAppeal,
                study = study,
                recruitStatus = RecruitStatus.PENDING,
            )
        }

        fun createByLeader(userId: UserId, study: Study): StudyRecruitment {
            return StudyRecruitment(
                userId = userId,
                appeal = LEADER_DEFAULT_APPEAL,
                study = study,
                recruitStatus = RecruitStatus.APPROVED
            )
        }

        private fun validateAppealLength(appeal: String) {
            if (appeal.isBlank()) {
                throw BusinessException(StudyDomainErrorCode.RECRUITMENT_APPEAL_EMPTY)
            }
            if (appeal.length > MAX_APPEAL_LENGTH) {
                throw BusinessException(StudyDomainErrorCode.RECRUITMENT_APPEAL_TOO_BIG)
            }
        }
    }

    fun updateAppeal(newAppeal: String) {
        val trimmedAppeal = newAppeal.trim()
        validateAppealLength(trimmedAppeal)
        this.appeal = trimmedAppeal
        recentUpdateAt()
    }

    fun updateRecruitStatus(newRecruitStatus: RecruitStatus) {
        validateStudyStatus(this.study.status)
        validateRecruitStatus(newRecruitStatus)
        this.recruitStatus = newRecruitStatus
        recentUpdateAt()
    }

    private fun recentUpdateAt() {
        this.updatedAt = LocalDateTime.now()
    }

    private fun validateRecruitStatus(recruitStatus: RecruitStatus) {
        val isValid = when (recruitStatus) {
            RecruitStatus.APPROVED,
            RecruitStatus.REJECTED -> this.recruitStatus == RecruitStatus.PENDING

            RecruitStatus.CANCELLED ->
                this.recruitStatus == RecruitStatus.PENDING || this.recruitStatus == RecruitStatus.APPROVED

            RecruitStatus.PENDING -> false
        }

        if (!isValid) {
            throw BusinessException(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE)
        }
    }

    private fun validateStudyStatus(studyStatus: StudyStatus) {
        if (studyStatus == StudyStatus.APPROVED || studyStatus == StudyStatus.REJECTED) {
            throw BusinessException(StudyDomainErrorCode.RECRUITMENT_STATUS_CANT_CHANGE_IN_DETERMINE)
        }
    }
}

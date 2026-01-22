package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
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
    @Column(nullable = false)
    val studyId: Long,
    appeal: String,
    recruitStatus: RecruitStatus,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @Column(nullable = false, length =MAX_APPEAL_LENGTH)
    var appeal: String = appeal
        protected set

    @Column(nullable = false)
    var recruitStatus: RecruitStatus = recruitStatus
        protected set

    @Column(nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    companion object {
        const val MAX_APPEAL_LENGTH = 100
        const val LEADER_DEFAULT_APPEAL = "스터디장 자동 참여"

        fun apply(userId: UserId, studyId: Long, appeal: String): StudyRecruitment {
            validateAppealLength(appeal)

            return StudyRecruitment(
                userId = userId,
                studyId = studyId,
                appeal = appeal,
                recruitStatus = RecruitStatus.PENDING
            )
        }

        fun createByLeader(userId: UserId, studyId: Long): StudyRecruitment {
            return StudyRecruitment(
                userId = userId,
                studyId = studyId,
                appeal = LEADER_DEFAULT_APPEAL,
                recruitStatus = RecruitStatus.APPROVED
            )
        }

        private fun validateAppealLength(appeal: String) {
            if (appeal.length > MAX_APPEAL_LENGTH) {
                throw BusinessException(StudyDomainErrorCode.RECRUITMENT_APPEAL_TOO_BIG)
            }
        }
    }

    fun updateAppeal(newAppeal: String) {
        validateAppealLength(newAppeal)
        this.appeal = newAppeal
        recentUpdateAt()
    }

    fun updateRecruitStatus(newRecruitStatus: RecruitStatus, studyStatus: StudyStatus) {
        validateStudyStatus(studyStatus)
        validateRecruitStatus(newRecruitStatus)
        this.recruitStatus = newRecruitStatus
        recentUpdateAt()
    }

    private fun recentUpdateAt(){
        this.updatedAt = LocalDateTime.now()
    }

    private fun validateRecruitStatus(recruitStatus: RecruitStatus) {
        when(recruitStatus) {
            RecruitStatus.PENDING -> {
                if (this.recruitStatus != RecruitStatus.CANCELLED) {
                    throw BusinessException(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE)
                }
            }

            RecruitStatus.APPROVED, RecruitStatus.REJECTED -> {
                if (this.recruitStatus != RecruitStatus.PENDING) {
                    throw BusinessException(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE)
                }
            }

            RecruitStatus.CANCELLED -> {
                if (this.recruitStatus != RecruitStatus.PENDING && this.recruitStatus != RecruitStatus.APPROVED) {
                    throw BusinessException(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE)
                }
            }
        }
    }

    private fun validateStudyStatus(studyStatus: StudyStatus){
        if (studyStatus == StudyStatus.APPROVED || studyStatus == StudyStatus.REJECTED) {
            throw BusinessException(StudyDomainErrorCode.RECRUITMENT_STATUS_CANT_CHANGE_IN_DETERMINE)
        }
    }
}
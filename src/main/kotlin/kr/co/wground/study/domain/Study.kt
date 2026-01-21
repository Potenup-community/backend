package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.constant.StudyStatus
import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.study.domain.constant.BudgetType
import kr.co.wground.study.domain.exception.StudyDomainErrorCode

@Entity
class Study(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    name: String,
    @Column(nullable = false)
    val leaderId: UserId,
    @Column(nullable = false)
    val trackId: TrackId,
    scheduleId: Long,
    description: String,
    status: StudyStatus,
    capacity: Int = RECOMMENDED_MAX_CAPACITY,
    budget: BudgetType,
    externalChatUrl: String,
    referenceUrl: String? = null,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @Column(nullable = false, length = 50)
    var name: String = name
        protected set

    @Column(nullable = false)
    var scheduleId: Long = scheduleId
        protected set

    @Column(length = 300)
    var description: String = description
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: StudyStatus = status
        protected set

    @Column(nullable = false)
    var currentMemberCount: Int = 1
        protected set

    @Column(nullable = false)
    var capacity: Int = capacity
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var budget: BudgetType = budget
        protected set

    @Column(nullable = false)
    var externalChatUrl: String = externalChatUrl
        protected set

    @Column(nullable = true)
    var referenceUrl: String? = referenceUrl
        protected set

    @Column(nullable = false)
    var updatedAt: LocalDateTime = updatedAt
        protected set

    companion object {
        const val MIN_CAPACITY = 2
        const val RECOMMENDED_MAX_CAPACITY = 10
        const val ABSOLUTE_MAX_CAPACITY = 60

        val URL_PATTERN = Regex("^(http|https)://.*$")
    }

    init {
        validateName(name)
        validateDescription(description)
        validateCapacity(capacity)
        validateUrl(externalChatUrl, referenceUrl)
    }

    fun increaseMemberCount() {
        if (this.status != StudyStatus.PENDING) {
            throw BusinessException(StudyDomainErrorCode.STUDY_NOT_RECRUITING)
        }
        if (this.currentMemberCount >= this.capacity) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CAPACITY_FULL)
        }

        this.currentMemberCount++
        checkAndChangeStatus()
    }

    fun updateStudyInfo(
        newName: String,
        newDescription: String,
        newCapacity: Int,
        newBudget: BudgetType,
        newChatUrl: String,
        newRefUrl: String?
    ) {
        validateCanUpdate()
        validateName(newName)
        validateDescription(newDescription)
        validateCapacity(newCapacity)
        validateUrl(newChatUrl, newRefUrl)

        validateCurrentMemberOverCapacity(newCapacity)

        this.name = newName
        this.description = newDescription
        this.capacity = newCapacity
        this.budget = newBudget
        this.externalChatUrl = newChatUrl
        this.referenceUrl = newRefUrl
        this.updatedAt = LocalDateTime.now()

        checkAndChangeStatus()
    }

    fun approve() {
        if (this.status != StudyStatus.CLOSED) {
            throw BusinessException(StudyDomainErrorCode.STUDY_MUST_BE_CLOSED_TO_APPROVE)
        }
        this.status = StudyStatus.APPROVED
    }

    fun reject() {
        this.status = StudyStatus.REJECTED
    }

    private fun checkAndChangeStatus() {
        if (this.status == StudyStatus.APPROVED || this.status == StudyStatus.REJECTED) {
            return
        }

        if (this.currentMemberCount >= this.capacity) {
            this.status = StudyStatus.CLOSED
        } else {
            this.status = StudyStatus.PENDING
        }
    }

    private fun validateCanUpdate() {
        if (this.status == StudyStatus.APPROVED || this.status == StudyStatus.REJECTED) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_AFTER_DETERMINED)
        }
    }

    private fun validateName(name: String) {
        if (name.trim().isBlank() || name.length > 50) {
            throw BusinessException(StudyDomainErrorCode.STUDY_NAME_INVALID)
        }
    }

    private fun validateDescription(description: String) {
        if (description.trim().isBlank() || description.length > 300) {
            throw BusinessException(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID)
        }
    }

    private fun validateCapacity(capacity: Int) {
        if (capacity < MIN_CAPACITY) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CAPACITY_TOO_SMALL)
        }
        if (capacity > ABSOLUTE_MAX_CAPACITY) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CAPACITY_TOO_BIG)
        }
    }

    private fun validateUrl(chatUrl: String, refUrl: String?) {
        if (!URL_PATTERN.matches(chatUrl)) {
            throw BusinessException(StudyDomainErrorCode.STUDY_URL_INVALID)
        }
        if (refUrl != null && !URL_PATTERN.matches(refUrl)) {
            throw BusinessException(StudyDomainErrorCode.STUDY_URL_INVALID)
        }
    }

    private fun validateCurrentMemberOverCapacity(newCapacity: Int) {
        if (newCapacity < this.currentMemberCount) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CAPACITY_CANNOT_LESS_THAN_CURRENT)
        }
    }
}
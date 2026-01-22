package kr.co.wground.study.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.constant.BudgetType
import kr.co.wground.study.domain.constant.StudyStatus
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import java.time.LocalDateTime

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
    schedule: StudySchedule,
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
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    var name: String = name
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    var schedule: StudySchedule = schedule
        protected set

    @Column(length = MAX_DESCRIPTION_LENGTH)
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

    @OneToMany(mappedBy = "study", cascade = [CascadeType.ALL], orphanRemoval = true)
    protected val _studyTags: MutableList<StudyTag> = ArrayList()
    val studyTags: List<StudyTag> get() = _studyTags.toList()

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
        const val MAX_TAG_COUNT = 5
        const val MAX_NAME_LENGTH = 50
        const val MAX_DESCRIPTION_LENGTH = 300
        val URL_PATTERN = Regex("^(http|https)://.*$")
    }

    init {
        validateName(name)
        validateDescription(description)
        validateCapacity(capacity)
        validateSchedule(schedule)
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
        newSchedule:StudySchedule,
        newBudget: BudgetType,
        newChatUrl: String,
        newRefUrl: String?
    ) {
        validateCanUpdate()
        validateName(newName)
        validateDescription(newDescription)
        validateCapacity(newCapacity)
        validateUrl(newChatUrl, newRefUrl)
        validateSchedule(newSchedule)
        validateCurrentMemberOverCapacity(newCapacity)

        this.name = newName
        this.description = newDescription
        this.capacity = newCapacity
        this.budget = newBudget
        this.schedule = newSchedule
        this.externalChatUrl = newChatUrl
        this.referenceUrl = newRefUrl
        this.updatedAt = LocalDateTime.now()

        checkAndChangeStatus()
    }

    fun addTag(tag: Tag) {
        if (this.studyTags.size >= MAX_TAG_COUNT) {
            throw BusinessException(StudyDomainErrorCode.STUDY_TAG_COUNT_EXCEEDED)
        }

        val isExist = this.studyTags.any { it.tag.id == tag.id }
        if (isExist) {
            return
        }

        val studyTag = StudyTag(study = this, tag = tag)
        this._studyTags.add(studyTag)
    }

    fun removeTag(tag: Tag) {
        this._studyTags.removeIf { it.tag.id == tag.id }
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

    fun validateHardDeletable() {
        if (this.status == StudyStatus.APPROVED || this.status == StudyStatus.REJECTED) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CANT_DELETE_STATUS_DETERMINE)
        }
    }

    private fun validateSchedule(schedule: StudySchedule) {
        if(!this.trackId.equals(schedule.trackId)) {
            throw BusinessException(StudyDomainErrorCode.STUDY_SCHEDULE_IS_NOT_IN_TRACK)
        }
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
        if (name.trim().isBlank() || name.length > MAX_NAME_LENGTH) {
            throw BusinessException(StudyDomainErrorCode.STUDY_NAME_INVALID)
        }
    }

    private fun validateDescription(description: String) {
        if (description.trim().isBlank() || description.length > MAX_DESCRIPTION_LENGTH) {
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
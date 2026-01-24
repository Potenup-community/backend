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
    externalChatUrl: String = DEFAULT_CHAT_URL,
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
    protected val _recruitments: MutableList<StudyRecruitment> = ArrayList()
    val recruitments: List<StudyRecruitment> get() = _recruitments.toList()

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
        const val DEFAULT_CHAT_URL = "https://www.kakaocorp.com/page/service/service/openchat"
        val URL_PATTERN = Regex("^(http|https)://.*$")
    }

    init {
        validateName(name)
        validateDescription(description)
        validateCapacity(capacity)
        schedule.validateTrackId(trackId)
        validateUrl(externalChatUrl, referenceUrl)
    }

    fun increaseMemberCount() {
        if (isOverRecruit()) {
            refreshStatus()
            throw BusinessException(StudyDomainErrorCode.STUDY_ALREADY_FINISH_TO_RECRUIT)
        }
        if (this.status != StudyStatus.PENDING) {
            throw BusinessException(StudyDomainErrorCode.STUDY_NOT_RECRUITING)
        }
        if (this.currentMemberCount >= this.capacity) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CAPACITY_FULL)
        }

        this.currentMemberCount++
        refreshStatus()
    }

    fun decreaseMemberCount() {
        if (this.currentMemberCount <= 1) {
            throw BusinessException(StudyDomainErrorCode.STUDY_MIN_MEMBER_REQUIRED)
        }

        this.currentMemberCount--

        refreshStatus()
    }

    fun updateStudyInfo(
        newName: String?,
        newDescription: String?,
        newCapacity: Int?,
        newBudget: BudgetType?,
        newSchedule: StudySchedule?,
        newChatUrl: String?,
        newRefUrl: String?,
        newTags: List<Tag>?,
        now: LocalDateTime = LocalDateTime.now()
    ) {
        validateCanUpdate()

        val resolvedBudget = newBudget ?: this.budget
        val resolvedChatUrl = newChatUrl ?: this.externalChatUrl
        val resolvedRefUrl = newRefUrl ?: this.referenceUrl

        validateUrl(resolvedChatUrl, resolvedRefUrl)

        this.budget = resolvedBudget
        this.externalChatUrl = resolvedChatUrl
        this.referenceUrl = resolvedRefUrl

        if (!schedule.isRecruitmentClosed(now)) {
            val resolvedName = newName ?: this.name
            val resolvedDescription = newDescription ?: this.description
            val resolvedCapacity = newCapacity ?: this.capacity

            validateName(resolvedName)
            validateDescription(resolvedDescription)
            validateCapacity(resolvedCapacity)
            validateCurrentMemberOverCapacity(resolvedCapacity)
            newSchedule.validateTrackId(this.trackId)
            this.name = resolvedName
            this.description = resolvedDescription
            this.capacity = resolvedCapacity

            if (newTags != null) {
                updateTags(newTags)
            }
        } else {
            if (newName != null || newDescription != null || newCapacity != null || newTags != null) {
                throw BusinessException(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_AFTER_DEADLINE)
            }
        }
        this.updatedAt = LocalDateTime.now()
        refreshStatus(now)
    }

    private fun updateTags(newTags: List<Tag>) {
        val tags = this._studyTags.iterator()
        while (tags.hasNext()) {
            val currentStudyTag = tags.next()
            if (newTags.none { it.id == currentStudyTag.tag.id }) {
                tags.remove()
            }
        }

        newTags.forEach { newTag ->
            val isAlreadyExist = this._studyTags.any { it.tag.id == newTag.id }
            if (!isAlreadyExist) {
                this.addTag(newTag)
            }
        }
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

    fun refreshStatus(now: LocalDateTime = LocalDateTime.now()) {
        if (this.status == StudyStatus.APPROVED || this.status == StudyStatus.REJECTED) {
            return
        }

        if (schedule.isRecruitmentClosed(now)) {
            if (this.currentMemberCount < MIN_CAPACITY) {
                this.status = StudyStatus.REJECTED
            } else {
                this.status = StudyStatus.CLOSED
            }
        } else {
            this.status = if (this.currentMemberCount >= this.capacity) {
                StudyStatus.CLOSED
            } else {
                StudyStatus.PENDING
            }
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

    private fun isOverRecruit(): Boolean {
        return this.schedule.recruitEndDate < LocalDateTime.now()
    }
}
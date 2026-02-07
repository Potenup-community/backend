package kr.co.wground.study.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
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
    scheduleId: Long,
    description: String,
    status: StudyStatus,
    capacity: Int = RECOMMENDED_MAX_CAPACITY,
    budget: BudgetType,
    budgetExplain: String,
    externalChatUrl: String = DEFAULT_CHAT_URL,
    referenceUrl: String? = null,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    var name: String = name
        protected set

    var scheduleId: Long = scheduleId
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

    @Column(nullable = false)
    var budgetExplain: String = budgetExplain

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
        const val MIN_NAME_LENGTH = 2
        const val MAX_DESCRIPTION_LENGTH = 300
        const val MIN_DESCRIPTION_LENGTH = 2
        const val MAX_BUDGET_EXPLAIN_LENGTH = 50
        const val MIN_BUDGET_EXPLAIN_LENGTH = 2
        const val DEFAULT_CHAT_URL = "https://www.kakaocorp.com/page/service/service/openchat"
        val URL_PATTERN = Regex("^(http|https)://.*$")
    }

    init {
        validateName(name)
        validateDescription(description)
        validateBudgetExplain(budgetExplain)
        validateCapacity(capacity)
        validateUrl(externalChatUrl, referenceUrl)
    }

    fun increaseMemberCount(recruitEndDate: LocalDateTime, isRecruitmentClosed: Boolean) {
        if (LocalDateTime.now() > recruitEndDate) {
            throw BusinessException(StudyDomainErrorCode.STUDY_ALREADY_FINISH_TO_RECRUIT)
        }

        if (this.currentMemberCount >= this.capacity) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CAPACITY_FULL)
        }

        if (this.status != StudyStatus.PENDING) {
            throw BusinessException(StudyDomainErrorCode.STUDY_NOT_RECRUITING)
        }

        this.currentMemberCount++
    }

    fun decreaseMemberCount(isRecruitmentClosed: Boolean) {
        if (this.currentMemberCount <= 1) {
            throw BusinessException(StudyDomainErrorCode.STUDY_MIN_MEMBER_REQUIRED)
        }

        this.currentMemberCount--
    }

    fun updateStudyInfo(
        newName: String,
        newDescription: String,
        newCapacity: Int,
        newScheduleId: Long,
        newBudget: BudgetType,
        newBudgetExplain: String,
        newChatUrl: String,
        newRefUrl: String?,
        newTags: List<Tag>?,
        isRecruitmentClosed: Boolean,
    ) {
        validateCanUpdate()
        validateBudgetExplain(newBudgetExplain)
        validateUrl(newChatUrl, newRefUrl)

        val isCoreInfoChanged = this.name != newName ||
                this.description != newDescription ||
                this.capacity != newCapacity ||
                isTagsChanged(newTags)

        if (isRecruitmentClosed && isCoreInfoChanged) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_AFTER_DEADLINE)
        }

        if (!isRecruitmentClosed) {
            validateName(newName)
            validateDescription(newDescription)
            validateCapacity(newCapacity)
            validateCurrentMemberOverCapacity(newCapacity)

            this.name = newName
            this.description = newDescription
            this.capacity = newCapacity

            if (newTags != null) {
                updateTags(newTags)
            }
        }

        this.budget = newBudget
        this.budgetExplain = newBudgetExplain
        this.scheduleId = newScheduleId
        this.externalChatUrl = newChatUrl
        this.referenceUrl = newRefUrl
        this.updatedAt = LocalDateTime.now()
    }

    private fun isTagsChanged(newTags: List<Tag>?): Boolean {
        if (newTags == null) return false
        val currentTagIds = this.studyTags.map { it.tag.id }.toSet()
        val newTagIds = newTags.map { it.id }.toSet()
        return currentTagIds != newTagIds
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

    fun close(recruitEndDate: LocalDateTime) {
        if (LocalDateTime.now().isBefore(recruitEndDate)) {
            throw BusinessException(StudyDomainErrorCode.RECRUITMENT_NOT_ENDED_YET)
        }

        this.status = StudyStatus.CLOSED
    }

    fun approve() {
        if (this.status != StudyStatus.CLOSED) {
            throw BusinessException(StudyDomainErrorCode.STUDY_MUST_BE_CLOSED_TO_APPROVE)
        }

        if (currentMemberCount < MIN_CAPACITY) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CANNOT_APPROVED_DUE_TO_NOT_ENOUGH_MEMBER)
        }

        this.status = StudyStatus.APPROVED
    }

    fun validateHardDeletable() {
        if (this.status == StudyStatus.APPROVED) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CANT_DELETE_STATUS_APPROVED)
        }
    }

    fun isLeader(userId: UserId): Boolean {
        return this.leaderId == userId
    }

    private fun validateCanUpdate() {
        if (this.status == StudyStatus.APPROVED) {
            throw BusinessException(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_AFTER_APPROVED)
        }
    }

    private fun validateName(name: String) {
        if (name.trim().length !in MIN_NAME_LENGTH..MAX_NAME_LENGTH) {
            throw BusinessException(StudyDomainErrorCode.STUDY_NAME_INVALID)
        }
    }

    private fun validateDescription(description: String) {
        if (description.trim().length !in MIN_DESCRIPTION_LENGTH..MAX_DESCRIPTION_LENGTH) {
            throw BusinessException(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID)
        }
    }

    private fun validateBudgetExplain(budgetExplain: String) {
        if (budgetExplain.trim().length !in MIN_BUDGET_EXPLAIN_LENGTH..MAX_BUDGET_EXPLAIN_LENGTH) {
            throw BusinessException(StudyDomainErrorCode.STUDY_BUDGET_EXPLAIN_INVALID)
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
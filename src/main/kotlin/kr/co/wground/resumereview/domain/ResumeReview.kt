package kr.co.wground.resumereview.domain

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Lob
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.resumereview.domain.vo.ResumeSection
import kr.co.wground.resumereview.exception.ResumeReviewErrorCode.NOT_ALLOW_BLANK_FIELD
import kr.co.wground.resumereview.exception.ResumeReviewErrorCode.TOO_LONG_FIELD
import java.time.LocalDateTime

@Entity
class ResumeReview protected constructor(
    @Id
    val id: Long = 0,
    val userId: UserId,
    val resumeReviewTitle: String,
    val jdUrl: String,
    @Column(unique = true, length = 64)
    val hash: String,
    @Embedded
    val resumeSections: ResumeSection,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    @Enumerated(EnumType.STRING)
    var status: ResumeReviewStatus = ResumeReviewStatus.PREPARED
        protected set
    @Lob
    @Column(nullable = true)
    var resultJson: String? = null
        protected set
    var completedAt: LocalDateTime? = null
        protected set
    @Column(length = 1000)
    var errorMessage: String? = null
        protected set

    init {
        validateTitle()
        validateJdUrl()
    }

    private fun validateTitle() {
        if (resumeReviewTitle.isBlank()) {
            throw BusinessException(
                NOT_ALLOW_BLANK_FIELD,
                messageMapper = { NOT_ALLOW_BLANK_FIELD.message.format(RESUME_REVIEW_TITLE) }
            )
        }

        if (resumeReviewTitle.length > REVIEW_TITLE_MAX_LENGTH) {
            throw BusinessException(
                TOO_LONG_FIELD,
                messageMapper = {
                    TOO_LONG_FIELD.message.format(RESUME_REVIEW_TITLE, REVIEW_TITLE_MAX_LENGTH)
                }
            )
        }
    }

    private fun validateJdUrl() {
        if (jdUrl.isBlank()) {
            throw BusinessException(
                NOT_ALLOW_BLANK_FIELD,
                messageMapper = { NOT_ALLOW_BLANK_FIELD.message.format(JD_URL) }
            )
        }

        if (jdUrl.length > JD_URL_MAX_LENGTH) {
            throw BusinessException(
                TOO_LONG_FIELD,
                messageMapper = {
                    TOO_LONG_FIELD.message.format(RESUME_REVIEW_TITLE, JD_URL_MAX_LENGTH)
                }
            )
        }
    }

    fun processed() {
        status = ResumeReviewStatus.PROCESSING
    }
    fun completed(resultJson: String, completedAt: LocalDateTime) {
        this.resultJson = resultJson
        status = ResumeReviewStatus.COMPLETED
        this.completedAt = completedAt
    }
    fun failed(errorMessage: String, completedAt: LocalDateTime) {
        status = ResumeReviewStatus.FAILED
        this.errorMessage = errorMessage
        this.completedAt = completedAt
    }

    fun isFinished() = status == ResumeReviewStatus.COMPLETED || status == ResumeReviewStatus.FAILED

    companion object {
        private const val REVIEW_TITLE_MAX_LENGTH = 100
        private const val RESUME_REVIEW_TITLE = "resumeReviewTitle"
        private const val JD_URL = "jdUrl"
        private const val JD_URL_MAX_LENGTH = 500

        fun create(
            userId: UserId,
            resumeReviewTitle: String,
            jdUrl: String,
            resumeSections: ResumeSection,
            hash: String
        ): ResumeReview {
            return ResumeReview(
                userId = userId,
                resumeReviewTitle = resumeReviewTitle.trim(),
                jdUrl = jdUrl.trim(),
                hash = hash,
                resumeSections = resumeSections
            )
        }
    }
}

enum class ResumeReviewStatus {
    PREPARED, PROCESSING, COMPLETED, FAILED;

    fun isFinished() =
        this == COMPLETED || this == FAILED

    fun isCompleted() = this == COMPLETED
}

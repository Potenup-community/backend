package kr.co.wground.resumereview.domain.vo

import jakarta.persistence.Embeddable
import jakarta.persistence.Lob
import kr.co.wground.exception.BusinessException
import kr.co.wground.resumereview.exception.ResumeReviewErrorCode.NOT_ALLOW_BLANK_FIELD
import kr.co.wground.resumereview.exception.ResumeReviewErrorCode.TOO_LONG_FIELD
import kr.co.wground.resumereview.exception.ResumeReviewErrorCode.TOO_LONG_TEXT_OF_ALL_SECTIONS

@Embeddable
class ResumeSection protected constructor(
    @Lob
    val summary: String,
    @Lob
    val skills: String,
    @Lob
    val experience: String,
    @Lob
    val education: String,
    @Lob
    val projects: String,
    @Lob
    val cert: String,
) {
    init {
        validateNotBlank("summary", summary)
        validateNotBlank("skills", skills)
        validateNotBlank("experience", experience)

        validateMaxLength("summary", summary, 800)
        validateMaxLength("skills", skills, 600)
        validateMaxLength("experience", experience, 6000)
        validateMaxLength("projects", projects, 4000)
        validateMaxLength("education", education, 800)
        validateMaxLength("cert", cert, 800)

        validateTotalLength()
    }

    private fun validateNotBlank(field: String, value: String) {
        if (value.isBlank()) {
            throw BusinessException(
                NOT_ALLOW_BLANK_FIELD,
                messageMapper = { NOT_ALLOW_BLANK_FIELD.message.format(field) }
            )
        }
    }

    private fun validateMaxLength(field: String, value: String, max: Int) {
        if (value.length > max) {
            throw BusinessException(
                TOO_LONG_FIELD,
                messageMapper = { TOO_LONG_FIELD.message.format(field, max) }
            )
        }
    }

    private fun validateTotalLength() {
        val total =
            summary.length +
                    skills.length +
                    experience.length +
                    education.length +
                    projects.length +
                    cert.length

        if (total > MAX_LENGTH) throw BusinessException(TOO_LONG_TEXT_OF_ALL_SECTIONS)
    }

    companion object {
        const val MAX_LENGTH = 12_000

        fun of(
            summary: String,
            skills: String,
            experience: String,
            education: String,
            projects: String,
            cert: String,
        ): ResumeSection {
            return ResumeSection(
                summary.trim(),
                skills.trim(),
                experience.trim(),
                education.trim(),
                projects.trim(),
                cert.trim()
            )
        }
    }
}

package kr.co.wground.gallery.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Lob
import kr.co.wground.exception.BusinessException
import kr.co.wground.gallery.domain.exception.ProjectErrorCode

@Embeddable
data class ProjectContent(
    @Column(nullable = false, length = MAX_TITLE_LENGTH)
    val title: String,
    @Lob
    @Column(nullable = false, columnDefinition = "TEXT", length = MAX_DESCRIPTION_LENGTH)
    val description: String,
) {
    companion object {
        const val MAX_TITLE_LENGTH = 100
        const val MAX_DESCRIPTION_LENGTH = 2000
    }

    init {
        validateTitle()
        validateDescription()
    }

    private fun validateTitle() {
        if (title.isBlank() || title.length > MAX_TITLE_LENGTH) {
            throw BusinessException(ProjectErrorCode.INVALID_PROJECT_TITLE)
        }
    }

    private fun validateDescription() {
        if (description.isBlank() || description.length > MAX_DESCRIPTION_LENGTH) {
            throw BusinessException(ProjectErrorCode.INVALID_PROJECT_DESCRIPTION)
        }
    }

    fun update(title: String?, description: String?): ProjectContent {
        return ProjectContent(
            title = title ?: this.title,
            description = description ?: this.description,
        )
    }
}

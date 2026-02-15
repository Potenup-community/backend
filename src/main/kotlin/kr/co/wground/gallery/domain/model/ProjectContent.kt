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
    @Column(nullable = false, columnDefinition = "TEXT")
    val description: String,
    @Lob
    @Column(columnDefinition = "TEXT")
    val additionalDescription: String? = null,
) {
    companion object {
        const val MAX_TITLE_LENGTH = 100
    }

    init {
        validateTitle()
    }

    private fun validateTitle() {
        require(title.isNotBlank() && title.length <= MAX_TITLE_LENGTH) {
            throw BusinessException(ProjectErrorCode.INVALID_PROJECT_TITLE)
        }
    }

    fun update(title: String?, description: String?, additionalDescription: String?): ProjectContent {
        return ProjectContent(
            title = title ?: this.title,
            description = description ?: this.description,
            additionalDescription = additionalDescription ?: this.additionalDescription,
        )
    }
}

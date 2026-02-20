package kr.co.wground.study.presentation.response

import org.springframework.data.domain.Page

data class CustomPageResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
) {
    companion object {
        fun <T> from(page: Page<T>): CustomPageResponse<T> {
            return CustomPageResponse(
                content = page.content,
                pageNumber = page.number,
                pageSize = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                hasNext = page.hasNext(),
            )
        }
    }
}

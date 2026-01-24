package kr.co.wground.study.presentation.response

import org.springframework.data.domain.Slice


data class CustomSliceResponse<T>(
    val content: List<T>,
    val pageNumber: Int,
    val pageSize: Int,
    val hasNext: Boolean
) {
    companion object {
        fun <T> from(slice: Slice<T>): CustomSliceResponse<T> =
            CustomSliceResponse(
                content = slice.content,
                pageNumber = slice.number,
                pageSize = slice.size,
                hasNext = slice.hasNext()
            )
    }
}

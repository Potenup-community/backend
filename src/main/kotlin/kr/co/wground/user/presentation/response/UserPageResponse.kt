package kr.co.wground.user.presentation.response

import kr.co.wground.user.application.operations.constant.START_NUMBER
import org.springframework.data.domain.Page

data class UserPageResponse<T>(
    val content: List<T>,
    val pageInfo: PageMetadata
){
    companion object{
        fun fromAdminSearchUserResponse(userPage : Page<AdminSearchUserResponse>): UserPageResponse<AdminSearchUserResponse> {
            return UserPageResponse(
                content = userPage.content,
                pageInfo = PageMetadata(
                    currentPage = userPage.number + START_NUMBER,
                    pageSize = userPage.size,
                    totalElements = userPage.totalElements,
                    totalPages = userPage.totalPages,
                    isFirst = userPage.isFirst,
                    isLast = userPage.isLast
            )
            )
        }
    }
}

data class PageMetadata(
    val currentPage: Int,
    val pageSize: Int,
    val totalElements: Long,
    val totalPages: Int,
    val isFirst: Boolean,
    val isLast: Boolean
)

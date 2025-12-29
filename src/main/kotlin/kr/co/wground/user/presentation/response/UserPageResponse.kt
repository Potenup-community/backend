package kr.co.wground.user.presentation.response

import kr.co.wground.user.application.operations.constant.START_NUMBER
import kr.co.wground.user.application.operations.dto.AdminSearchUserDto
import org.springframework.data.domain.Page

data class UserPageResponse<T>(
    val content: List<T>,
    val pageInfo: PageMetadata
){
    companion object{
        fun fromAdminSearchUserDto(userQueryPage : Page<AdminSearchUserDto>): UserPageResponse<AdminSearchUserDto> {
            return UserPageResponse(
                content = userQueryPage.content,
                pageInfo = PageMetadata(
                    currentPage = userQueryPage.number + START_NUMBER,
                    pageSize = userQueryPage.size,
                    totalElements = userQueryPage.totalElements,
                    totalPages = userQueryPage.totalPages,
                    isFirst = userQueryPage.isFirst,
                    isLast = userQueryPage.isLast
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

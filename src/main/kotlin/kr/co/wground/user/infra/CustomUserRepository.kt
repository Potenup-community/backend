package kr.co.wground.user.infra

import kr.co.wground.user.presentation.request.UserSearchRequest
import kr.co.wground.user.presentation.response.UserListResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomUserRepository {
    fun searchUsers(condition: UserSearchRequest, pageable: Pageable): Page<UserListResponse>
}
package kr.co.wground.user.application.operations

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.response.UserResponse
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {
    override fun getMyInfo(userId: UserId): UserResponse {
        val user = userRepository.findByIdOrNull(userId) ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)
        return UserResponse(
            id = user.userId,
            name = user.name,
            email = user.email,
            trackId = user.trackId,
            profileImageUrl = user.profileImageUrl,
            role = user.role,
        )
    }
}
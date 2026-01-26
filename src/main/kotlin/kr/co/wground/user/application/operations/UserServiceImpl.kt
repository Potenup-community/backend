package kr.co.wground.user.application.operations

import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.exception.UserServiceErrorCode
import kr.co.wground.user.application.operations.dto.MyInfoDto
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserServiceImpl(
    private val userRepository: UserRepository
) : UserService {

    @Transactional(readOnly = true)
    override fun getMyInfo(userId: UserId): MyInfoDto {
        val user = userRepository.findUserAndTrack(userId)
            ?: throw BusinessException(UserServiceErrorCode.USER_NOT_FOUND)

        if (user.status != UserStatus.ACTIVE) {
            throw BusinessException(UserServiceErrorCode.INACTIVE_USER)
        }
        return MyInfoDto.from(user)
    }

    @Transactional(readOnly = true)
    override fun getUsersForMention(size: Int, cursorId: Long?): List<UserDisplayInfoDto> {
        return userRepository.findUserDisplayInfosForMention(size, cursorId)
    }
}

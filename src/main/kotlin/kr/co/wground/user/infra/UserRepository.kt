package kr.co.wground.user.infra

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserStatus
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>, CustomUserRepository {
    fun findByEmail(email: String): User?
    fun existsUserByEmail(email: String): Boolean
    fun countByStatus(status: UserStatus): Long
    fun findByUserIdIn(userIds: List<UserId>): List<User>
    fun findByNameInAndTrackIdIn(names: List<String>, trackIds: List<TrackId>): List<User>
    fun existsByPhoneNumber(phoneNumber: String): Boolean
}

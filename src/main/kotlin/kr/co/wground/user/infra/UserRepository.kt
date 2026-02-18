package kr.co.wground.user.infra

import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long>, CustomUserRepository {
    fun findByEmail(email: String): User?
    fun existsUserByEmail(email: String): Boolean
    fun countByStatus(status: UserStatus): Long
    fun findByUserIdIn(userIds: List<UserId>): List<User>
    fun existsByPhoneNumber(phoneNumber: String): Boolean

    @Query("select u from User u where u.role = kr.co.wground.user.domain.constant.UserRole.ADMIN")
    fun findAllByRole(): List<User>
}

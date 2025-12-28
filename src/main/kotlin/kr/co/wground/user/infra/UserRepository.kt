package kr.co.wground.user.infra

import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserStatus
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>, CustomUserRepository{
    fun findByEmail(email: String): User?
    fun existsUserByEmail(email:String): Boolean
    fun countByStatus(status: UserStatus): Long
}

package kr.co.wground.user.infra

import jakarta.persistence.LockModeType
import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface UserRepository : JpaRepository<User, Long>, CustomUserRepository{
    fun findByEmail(email: String): User?
    fun existsUserByEmail(email:String): Boolean
    fun countByStatus(status: UserStatus): Long

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.userId = :userId")
    fun findByIdWithLock(@Param("userId") userId: UserId): User?
}

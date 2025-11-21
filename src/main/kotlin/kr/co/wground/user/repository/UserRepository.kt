package kr.co.wground.user.repository

import kr.co.wground.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>{
    fun findByEmail(email: String): User?
    fun existsUserByEmail(email: String): Boolean
}
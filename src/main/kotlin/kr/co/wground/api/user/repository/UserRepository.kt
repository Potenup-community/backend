package kr.co.wground.api.user.repository

import kr.co.wground.api.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>{
    fun findByEmail(email: String): User?
    fun existsUserByEmail(email: String): Boolean
}
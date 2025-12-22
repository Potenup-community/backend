package kr.co.wground.user.utils.defaultimage.infra

import kr.co.wground.user.utils.defaultimage.domain.UserProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserProfileRepository: JpaRepository<UserProfile, Long> {
}
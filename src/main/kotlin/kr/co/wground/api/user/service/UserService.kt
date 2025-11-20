package kr.co.wground.api.user.service

import jakarta.transaction.Transactional
import kr.co.wground.api.exception.BusinessException
import kr.co.wground.api.user.domain.RequestSignup
import kr.co.wground.api.user.domain.User
import kr.co.wground.api.user.domain.constant.UserSignupStatus
import kr.co.wground.api.user.domain.constant.UserStatus
import kr.co.wground.api.user.repository.RequestSignupRepository
import kr.co.wground.api.user.repository.UserRepository
import kr.co.wground.api.user.controller.dto.AdditionalInfoRequest
import kr.co.wground.api.user.service.dto.GoogleRequestDto
import kr.co.wground.api.user.service.exception.UserServiceErrorCode
import org.springframework.stereotype.Service

@Service
class UserService(
    private val requestSignupRepository: RequestSignupRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun upsertRequestSignup(googleProfile: GoogleRequestDto): RequestSignup {
        val existing = requestSignupRepository.findByEmail(googleProfile.email)
        return if (existing != null) {
            existing
        } else {
            requestSignupRepository.save(
            RequestSignup(
                affiliationId = googleProfile.affiliationId ?: 0L,
                email = googleProfile.email,
                name = googleProfile.name,
                phoneNumber = googleProfile.phoneNumber ?: "",
                provider = googleProfile.provider
                )
            )
        }
    }

    @Transactional
    fun saveAdditionalInfo(email: String, request: AdditionalInfoRequest) {
        val pending = requestSignupRepository.findByEmail(email)
            ?: throw BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND)

        val updated = RequestSignup(
            id = pending.id,
            affiliationId = request.affiliationId,
            email = pending.email,
            name = pending.name,
            phoneNumber = request.phoneNumber,
            provider = pending.provider,
            requestStatus = UserSignupStatus.PENDING
        )
        requestSignupRepository.save(updated)
    }

    @Transactional
    fun approve(id: Long, status: UserSignupStatus) {
        val request = requestSignupRepository.findById(id)
            .orElseThrow { BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND) }

        val user = User(
            affiliationId = request.affiliationId,
            role = UserStatus.from(status).name,
            email = request.email,
            name = request.name,
            phoneNumber = request.phoneNumber,
        )

        userRepository.save(user)
        requestSignupRepository.delete(request)
    }

    @Transactional
    fun reject(id: Long) {
        val request : RequestSignup = requestSignupRepository.findById(id)
            .orElseThrow { BusinessException(UserServiceErrorCode.REQUEST_SIGNUP_NOT_FOUND) }

        val rejected = RequestSignup(
            id = request.id,
            affiliationId = request.affiliationId,
            email = request.email,
            name = request.name,
            phoneNumber = request.phoneNumber,
            provider = request.provider,
            requestStatus = UserSignupStatus.REJECTED
        )
        requestSignupRepository.save(rejected)
    }
}
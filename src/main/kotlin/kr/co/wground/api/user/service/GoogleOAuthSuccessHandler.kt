package kr.co.wground.api.user.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.wground.api.user.domain.constant.UserSignupStatus
import kr.co.wground.api.user.domain.constant.UserStatus
import kr.co.wground.api.user.repository.UserRepository
import kr.co.wground.api.user.service.dto.GoogleRequestDto
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class GoogleOAuthSuccessHandler(
    private val userService: UserService,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val oAuth2User = authentication.principal as DefaultOAuth2User
        val googleProfile = GoogleRequestDto(
            email = oAuth2User.getAttribute("email")!!,
            name = oAuth2User.getAttribute("name")!!,
            provider = authentication.authorities.first().authority,
            phoneNumber = null,
            affiliationId = null
        )

        val requestSignup = userService.upsertRequestSignup(googleProfile)
        val status = determineStatus(requestSignup.email)

        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.writer.write(objectMapper.writeValueAsString(mapOf(
            "email" to googleProfile.email,
            "status" to status,
            "requiresAdditionalInfo" to (status == UserSignupStatus.PENDING.name)
        )))
    }

    private fun determineStatus(email: String): String {
        val user = userRepository.findByEmail(email)
        return ""
    }
}
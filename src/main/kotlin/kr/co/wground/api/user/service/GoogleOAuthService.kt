package kr.co.wground.api.user.service

import kr.co.wground.api.user.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class GoogleOAuthService(private val userRepository: UserRepository) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId

        val attributes = oAuth2User.attributes
        val email = attributes["email"] as String
        val name = attributes["name"] as String

        val user = userRepository.findByEmail(email)

        val authorities = if (user != null) {
            listOf(SimpleGrantedAuthority(user.role))
        } else {
            listOf(SimpleGrantedAuthority("ROLE_GUEST"))
        }
        return DefaultOAuth2User(
            authorities,
            attributes,
            "email" // nameAttributeKey
        )
    }
}


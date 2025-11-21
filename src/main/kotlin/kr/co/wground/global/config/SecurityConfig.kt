package kr.co.wground.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig(
    private val googleOAuthService: GoogleOAuthService,
    private val googleOAuthSuccessHandler: GoogleOAuthSuccessHandler
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.anyRequest().permitAll()
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .oauth2Login { oauth ->
                oauth.userInfoEndpoint { it.userService(googleOAuthService) }
                oauth.successHandler(googleOAuthSuccessHandler)
            }

        return http.build()
    }
}

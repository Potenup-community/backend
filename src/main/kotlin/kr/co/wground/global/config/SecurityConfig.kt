package kr.co.wground.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                // (TODO) 경로별 권한 설정
                it.anyRequest().permitAll()
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }


            //.oauth2Login { oauth ->
                // (TODO) Oauth2
                // 여기에 등록해야 합니다. (지금은 기본 설정 사용)
                // oauth.userInfoEndpoint { it.userService(customOAuth2UserService) }
                // oauth.successHandler(oAuth2LoginSuccessHandler)
            //}

        return http.build()
    }
}

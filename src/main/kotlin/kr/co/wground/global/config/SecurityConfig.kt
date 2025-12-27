package kr.co.wground.global.config

import kr.co.wground.exception.handler.CustomAccessDeniedHandler
import kr.co.wground.exception.handler.CustomAuthenticationEntryPoint
import kr.co.wground.global.jwt.JwtAuthenticationFilter
import kr.co.wground.image.policy.UploadPolicy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val uploadPolicy: UploadPolicy,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/v1/users/signup",
                    "/api/v1/auth/login",
                    "/api/v1/admin/tracks",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/actuator/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.accessDeniedHandler(customAccessDeniedHandler)
                it.authenticationEntryPoint(customAuthenticationEntryPoint)
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring()
                .requestMatchers("/assets/**")
                .requestMatchers("${uploadPolicy.publicBasePath}/**")
        }
    }
}

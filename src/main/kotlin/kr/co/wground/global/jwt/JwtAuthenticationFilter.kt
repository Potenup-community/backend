package kr.co.wground.global.jwt

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.wground.user.infra.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.security.SignatureException

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
) : OncePerRequestFilter() {
    private val log = LoggerFactory.getLogger(this.javaClass)!!

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request) ?: return filterChain.doFilter(request, response)

        try {
            val userId = jwtProvider.getUserId(token)
            val user = userRepository.findByIdOrNull(userId)
            val principle = UserPrincipal(userId)
            if (user != null) {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                val authentication = UsernamePasswordAuthenticationToken(principle, null, authorities)

                SecurityContextHolder.getContext().authentication = authentication
            }

        } catch (e: SignatureException) {
            log.warn("Invalid JWT signature.")
        } catch (e: MalformedJwtException) {
            log.warn("Invalid JWT token.")
        } catch (e: ExpiredJwtException) {
            log.warn("Expired JWT token.")
            filterChain.doFilter(request, response)
            return
        } catch (e: UnsupportedJwtException) {
            log.warn("Unsupported JWT token.")
        } catch (e: IllegalArgumentException) {
            log.warn("JWT claims string is empty.")
        } catch (e: Exception) {
            log.error("An unexpected error occurred during JWT validation.", e)
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
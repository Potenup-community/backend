package kr.co.wground.global.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.jwt.constant.HEADER_NAME
import kr.co.wground.global.jwt.constant.SUBSTRING_INDEX
import kr.co.wground.global.jwt.constant.TOKEN_START
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.presentation.dto.TokenType
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.WebUtils

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = resolveTokenFromHeader(request) ?: resolveTokenFromCookie(request)
            if(token == null){
                return filterChain.doFilter(request, response)
            }


            val userId = jwtProvider.validateAccessToken(token)

            val user = userRepository.findByIdOrNull(userId)
            val principle = UserPrincipal(userId)

            if (user != null) {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                val authentication = UsernamePasswordAuthenticationToken(principle, null, authorities)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (ex: BusinessException) {
            setErrorResponse(response, ex)
            return
        }
        filterChain.doFilter(request, response)
    }

    private fun resolveTokenFromHeader(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(HEADER_NAME)
        return if (bearerToken != null && bearerToken.startsWith(TOKEN_START)) {
            bearerToken.substring(SUBSTRING_INDEX)
        } else {
            null
        }
    }

    private fun resolveTokenFromCookie(request: HttpServletRequest): String? {
        val cookie = WebUtils.getCookie(request, TokenType.ACCESS.tokenType)
        return if (cookie != null && cookie.value.isNotBlank()) {
            cookie.value
        } else {
            null
        }
    }

    private fun setErrorResponse(
        response: HttpServletResponse,
        e: BusinessException
    ) {
        response.status = e.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        response.writer.write(objectMapper.writeValueAsString(ErrorResponse.of(e, emptyList())))
    }
}

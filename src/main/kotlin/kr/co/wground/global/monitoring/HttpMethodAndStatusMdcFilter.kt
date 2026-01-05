package kr.co.wground.global.monitoring

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jboss.logging.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class HttpMethodAndStatusMdcFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            MDC.put("http_method", request.method)
            filterChain.doFilter(request, response)
        } finally {
            MDC.put("http_status", response.status.toString())
            MDC.clear() // 누수 방지를 위해
        }
    }
}
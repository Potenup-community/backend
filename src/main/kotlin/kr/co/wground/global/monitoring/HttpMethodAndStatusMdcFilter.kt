package kr.co.wground.global.monitoring

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jboss.logging.MDC
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class HttpMethodAndStatusMdcFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

        val beginTimeNs = System.nanoTime()

        try {
            MDC.put("http_method", request.method)
            filterChain.doFilter(request, response)
        } finally {
            val elapsedTimeMs = (System.nanoTime() - beginTimeNs) / 1_000_000
            MDC.put("http_status", response.status.toString())
            MDC.put("elapsed_time_ms", elapsedTimeMs)

            // 응답 이후 로그를 한 번 찍어서 http status 및 요청 처리 시간 기록
            logger.info("HTTP_RESPONSE_LOG")

            MDC.clear() // 누수 방지를 위해
        }
    }
}
package kr.co.wground.global.monitoring

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.jboss.logging.MDC
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
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

        val startTimeMs = System.currentTimeMillis()

        val exactUrl: String = request.requestURL.toString()

        try {
            MDC.put("http_method", request.method)
            filterChain.doFilter(request, response)
        } finally {
            val elapsedTimeMs = System.currentTimeMillis() - startTimeMs
            MDC.put("http_status", response.status.toString())
            MDC.put("elapsed_time_ms", elapsedTimeMs)

            val errCode: String? = request.getAttribute(MonitoringConstants.ERROR_CODE_FOR_LOG) as? String
            val errMessage: String? = request.getAttribute(MonitoringConstants.ERROR_MESSAGE_FOR_LOG) as? String

            // 응답 이후 로그를 한 번 찍어서 http status 및 요청 처리 시간 기록
            if (HttpStatusCode.valueOf(response.status).is2xxSuccessful) {
                logger.info("[SUCCESS] HTTP_RESPONSE: exactUrl=${exactUrl}")
            } else if (HttpStatusCode.valueOf(response.status).is5xxServerError) {
                val stackTrace = request.getAttribute(MonitoringConstants.EXCEPTION_FOR_LOG) as Exception
                logger.error("[ERROR] HTTP_RESPONSE: exactUrl=${exactUrl}, errCode=${errCode}, errMessage=${errMessage}", stackTrace)
            } else {
                logger.warn("[WARNING] HTTP_RESPONSE: exactUrl=${exactUrl}, errCode=${errCode}, errMessage=${errMessage}")
            }

            MDC.clear() // 누수 방지를 위해
        }
    }
}
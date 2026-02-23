package kr.co.wground.resumereview.application.hash

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import kr.co.wground.resumereview.application.command.dto.CreateResumeReviewDto
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

@Component
class RequestHashCalculator(
    private val objectMapper: ObjectMapper
) {

    private val canonicalMapper = objectMapper.copy().apply {
        configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    }

    private fun canonicalize(dto: CreateResumeReviewDto): String {
        val payload = mapOf(
            "jdUrl" to dto.jdUrl.trim(),
            "sections" to mapOf(
                "summary" to dto.summary.trim(),
                "skills" to dto.skills.trim(),
                "experience" to dto.experience.trim(),
                "education" to dto.education.trim(),
                "projects" to dto.projects.trim(),
                "cert" to dto.cert.trim()
            ),
            "model" to "gpt-5",
            "promptVersion" to "v1",
            "pipelineVersion" to "v1",
            "schemaVersion" to "v1"
        )

        return canonicalMapper.writeValueAsString(payload)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun calculate(dto: CreateResumeReviewDto): String {
        val canonical = canonicalize(dto)
        return sha256(canonical)
    }
}

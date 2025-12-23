package kr.co.wground.user.presentation.response

import org.springframework.core.io.Resource
import java.time.LocalDateTime
import java.time.ZoneId

data class ProfileResourceResponse(
    val resource: Resource,
    val contentType: String,
    val modifiedAt: LocalDateTime
) {
    val lastModifiedAt: Long
        get() = modifiedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

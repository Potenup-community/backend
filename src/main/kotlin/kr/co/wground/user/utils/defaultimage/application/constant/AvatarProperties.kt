package kr.co.wground.user.utils.defaultimage.application.constant

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path
import java.time.Duration

@ConfigurationProperties(prefix = "app.avatar")
data class AvatarProperties(
    val uploadPath: Path,
    val webPathPrefix: String,
    val retryMaxAttempts: Int,
    val retryDelay: Duration,
    val defaultSize: Int,
    val placeholderPath: String,
    val imageQuality: Int,
)

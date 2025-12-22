package kr.co.wground.user.utils.defaultimage.application.constant

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path
import java.time.Duration

@ConfigurationProperties(prefix = "app.avatar")
data class AvatarProperties(
    val uploadPath: Path,
    val webPathPrefix: String,
    val retryMaxAttempts: Int = 3,
    val retryDelay: Duration,
    val defaultSize: Int,
    val placeholderPath: String,
    val imageQuality: Int,
) {
    init {
        require(defaultSize > 0) { "defaultSize must be positive" }
        require(imageQuality in 1..2000) { "imageQuality out of range" }
    }
}

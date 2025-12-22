package kr.co.wground.user.utils.defaultimage.application.constant

import org.springframework.boot.context.properties.ConfigurationProperties
import java.nio.file.Path
import java.time.Duration

@ConfigurationProperties(prefix = "app.avatar")
data class AvatarProperties(
    val uploadPath: Path = Path.of(System.getProperty("user.home"), "depth", "profiles"),
    val webPathPrefix: String = "/images/profiles/",
    val retryMaxAttempts: Int = 3,
    val retryDelay: Duration = Duration.ofSeconds(2),
    val defaultSize: Int = 40,
    val placeholderPath: String = "/images/static-default.png",
    val imageQuality: Int = 300,
) {
    init {
        require(defaultSize > 0) { "defaultSize must be positive" }
        require(imageQuality in 1..2000) { "imageQuality out of range" }
    }
}

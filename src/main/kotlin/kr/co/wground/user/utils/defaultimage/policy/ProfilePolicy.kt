package kr.co.wground.user.utils.defaultimage.policy

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.profile")
class ProfilePolicy(
    val localDir: String,
    val webPathPrefix: String,
    val maxBytes: Long,
    val allowedExts: Set<String>,
    val baseUrl: String,
) {
}

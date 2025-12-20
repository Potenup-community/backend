package kr.co.wground.image.policy

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.upload")
class UploadPolicy(
    val maxImageBytes: Long,
    val maxGifBytes: Long,
    allowedMimeTypes: String,
    val localDir: String,
    val publicBasePath: String,
    val cachePeriod: Int
) {
    val allowedMimeTypeSet: Set<String> =
        allowedMimeTypes.split(",").map { it.trim().lowercase() }.toSet()
}

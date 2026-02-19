package kr.co.wground.shop.infra.image

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.item")
class ItemPolicy(
    val localDir: String,
    val webPathPrefix: String,
    val maxBytes: Long,
    val allowedExts: Set<String>,
    val baseUrl: String,
)
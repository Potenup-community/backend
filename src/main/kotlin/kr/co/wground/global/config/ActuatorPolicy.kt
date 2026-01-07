package kr.co.wground.global.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "management.endpoints.web")
class ActuatorPolicy(
    val basePath: String,
)
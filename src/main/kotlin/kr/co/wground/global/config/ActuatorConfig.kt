package kr.co.wground.global.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ActuatorPolicy::class)
class ActuatorConfig()
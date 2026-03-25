package kr.co.wground.global.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Expiry
import kr.co.wground.user.presentation.response.TokenResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.time.LocalDateTime

@Configuration
class CacheConfig {
    @Bean
    fun rotationCache(): Cache<String, TokenResponse> {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .maximumSize(1000)
            .build()
    }

    @Bean
    fun attendanceDailyCheckCache(): Cache<Long, Boolean> {
        return Caffeine.newBuilder()
            .expireAfter(object : Expiry<Long, Boolean> {
                override fun expireAfterCreate(key: Long, value: Boolean, currentTime: Long): Long {
                    val now = LocalDateTime.now()
                    val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
                    return Duration.between(now, midnight).toNanos()
                }

                override fun expireAfterUpdate(key: Long, value: Boolean, currentTime: Long, currentDuration: Long) = currentDuration
                override fun expireAfterRead(key: Long, value: Boolean, currentTime: Long, currentDuration: Long) = currentDuration
            })
            .maximumSize(10_000)
            .build()
    }
}
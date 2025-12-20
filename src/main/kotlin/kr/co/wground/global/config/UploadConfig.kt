package kr.co.wground.global.config

import kr.co.wground.image.policy.UploadPolicy
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path

@Configuration
@EnableConfigurationProperties(UploadPolicy::class)
class UploadConfig(
    private val props: UploadPolicy,
): WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val location = Path.of(props.localDir).toAbsolutePath().normalize().toUri().toString()
        println(location)
        registry.addResourceHandler("${props.publicBasePath.trimEnd('/')}/**")
            .addResourceLocations(location)
            .setCachePeriod(props.cachePeriod)
    }
}

package kr.co.wground.global.config

import kr.co.wground.global.config.resolver.UserIdArgumentResolver
import kr.co.wground.global.monitoring.HttpRouteMdcInterceptor
import kr.co.wground.image.policy.UploadPolicy
import kr.co.wground.user.utils.defaultimage.policy.ProfilePolicy
import org.springframework.context.annotation.Configuration
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val userIdArgumentResolver: UserIdArgumentResolver,
    private val uploadPolicy: UploadPolicy,
    private val profilePolicy: ProfilePolicy,
    private val httpRouteMdcInterceptor: HttpRouteMdcInterceptor
) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins(
                "http://localhost:5173",
                "https://potenup-depth.vercel.app",
                "https://depth-preview.vercel.app",
            )
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/assets/**")
            .addResourceLocations("classpath:/assets/")

        registry.addResourceHandler("${uploadPolicy.publicBasePath}/**")
            .addResourceLocations("file:${uploadPolicy.localDir}/")
            .setCachePeriod(uploadPolicy.cachePeriod)

        registry.addResourceHandler("${profilePolicy.webPathPrefix}/**")
            .addResourceLocations("file:${profilePolicy.localDir}/")
            .setCachePeriod(uploadPolicy.cachePeriod)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        val pageResolver = PageableHandlerMethodArgumentResolver()
        pageResolver.setOneIndexedParameters(true)

        resolvers.add(pageResolver)
        resolvers.add(userIdArgumentResolver)

        super.addArgumentResolvers(resolvers)
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(httpRouteMdcInterceptor)
    }
}

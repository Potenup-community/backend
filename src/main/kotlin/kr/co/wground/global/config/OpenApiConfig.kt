package kr.co.wground.global.config

import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.user.docs.UserSwaggerErrorExample
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.AntPathMatcher

@Configuration
class OpenApiConfig(
    private val actuatorPolicy: ActuatorPolicy,
) {
    @Bean
    fun openAPI(): OpenAPI {
        val securitySchemeName = "bearerAuth"

        return OpenAPI()
            .info(
                Info()
                    .title("Potenup API")
                    .description("Potenup 서비스 API 문서")
                    .version("v1")
            )
            .addSecurityItem(
                SecurityRequirement().addList(securitySchemeName)
            )
            .components(
                io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name("Authorization")
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
    }

    @Bean
    fun globalOpenApiCustomizer(): GlobalOpenApiCustomizer {
        val excludedPaths = listOf(
            "/api/v1/auth/login",
            "/api/v1/users/signup",
            "${actuatorPolicy.basePath}/**"
        )
        val pathMatcher = AntPathMatcher()

        return GlobalOpenApiCustomizer { openApi ->
            val resolvedSchema = ModelConverters.getInstance()
                .readAllAsResolvedSchema(ErrorResponse::class.java).schema

            openApi.paths.forEach { (path, pathItem) ->
                if (excludedPaths.any { pattern -> pathMatcher.match(pattern, path) }) {
                    return@forEach
                }

                pathItem.readOperations().forEach { operation ->
                    val apiResponses = operation.responses
                    val unauthorizedResponse = ApiResponse()
                        .description("인증 실패")
                        .content(
                            Content().addMediaType(
                                "application/json",
                                MediaType().schema(resolvedSchema)
                                    .addExamples(
                                        "INVALID_TOKEN",
                                        Example().value(UserSwaggerErrorExample.Unauthorized.INVALID_ACCESS_TOKEN)
                                    )
                                    .addExamples(
                                        "INVALID_REFRESH_TOKEN",
                                        Example().value(UserSwaggerErrorExample.Unauthorized.INVALID_REFRESH_TOKEN)
                                    )
                                    .addExamples(
                                        "TOKEN_EXPIRED",
                                        Example().value(UserSwaggerErrorExample.Unauthorized.TOKEN_EXPIRED)
                                    )
                            )
                        )
                    apiResponses.addApiResponse("401", unauthorizedResponse)
                }
            }
        }
    }
}

package kr.co.wground.user.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.user.docs.UserSwaggerErrorExample
import kr.co.wground.user.presentation.response.ProfileResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.context.request.WebRequest
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Profile", description = "유저 프로필 이미지 API")
interface ProfileApi {

    @Operation(
        summary = "프로필 이미지 조회",
        description = "유저 ID에 해당하는 프로필 이미지를 조회합니다. 브라우저 캐싱(304)을 지원합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "이미지 조회 성공",
                content = [Content(mediaType = "image/jpeg", schema = Schema(type = "string", format = "binary"))]
            ),
            ApiResponse(
                responseCode = "304",
                description = "변경 없음 (캐시 유지)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "유저 또는 이미지를 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "USER_NOT_FOUND", value = UserSwaggerErrorExample.NotFound.USER_NOT_FOUND),
                        ExampleObject(
                            name = "PROFILE_NOT_FOUND",
                            value = UserSwaggerErrorExample.NotFound.PROFILE_NOT_FOUND
                        )
                    ]
                )]
            )
        ]
    )
    fun getMyProfile(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
        @Parameter(hidden = true) request: WebRequest
    ): ResponseEntity<ProfileResponse>


    @Operation(
        summary = "프로필 이미지 저장",
        description = "유저 ID에 업로드한 프로필 이미지를 저장합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "프로필 이미지 저장 성공",
                content = [Content(mediaType = "image/jpeg", schema = Schema(type = "string", format = "binary"))]
            ),
            ApiResponse(
                responseCode = "304",
                description = "변경 없음 (캐시 유지)"
            ),
            ApiResponse(
                responseCode = "404",
                description = "유저를 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "USER_NOT_FOUND", value = UserSwaggerErrorExample.NotFound.USER_NOT_FOUND),
                        ExampleObject(
                            name = "PROFILE_NOT_FOUND",
                            value = UserSwaggerErrorExample.NotFound.PROFILE_NOT_FOUND
                        )
                    ]
                )]
            )
        ]
    )
    fun uploadProfileImage(
        @RequestPart("file") file: MultipartFile, @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId
    ): ResponseEntity<Unit>


    @Operation(
        summary = "프로필 이미지 삭제",
        description = "유저 ID에 해당하는 프로필 이미지를 삭제합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "프로필 이미지 삭제 성공",
                content = [Content(mediaType = "image/jpeg", schema = Schema(type = "string", format = "binary"))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "유저를 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(name = "USER_NOT_FOUND", value = UserSwaggerErrorExample.NotFound.USER_NOT_FOUND),
                        ExampleObject(
                            name = "PROFILE_NOT_FOUND",
                            value = UserSwaggerErrorExample.NotFound.PROFILE_NOT_FOUND
                        )
                    ]
                )]
            )
        ]
    )
    fun deleteProfile(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId
    ): ResponseEntity<Unit>
}
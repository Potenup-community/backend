package kr.co.wground.notification.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.common.response.ErrorResponse
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.notification.docs.NotificationSwaggerErrorExample
import kr.co.wground.notification.docs.NotificationSwaggerResponseExample
import kr.co.wground.notification.presentation.response.NotificationsResponse
import kr.co.wground.notification.presentation.response.UnreadCountResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity

@Tag(name = "Notifications", description = "알림 API")
interface NotificationApi {

    @Operation(
        summary = "알림 목록 조회",
        description = "현재 로그인한 사용자의 알림 목록을 페이지 단위로 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = NotificationsResponse::class),
                    examples = [
                        ExampleObject(name = "NOTIFICATIONS", value = NotificationSwaggerResponseExample.NOTIFICATIONS)
                    ]
                )]
            ),
        ]
    )
    fun getNotifications(
        @ParameterObject
        @PageableDefault(size = 20)
        pageable: Pageable,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
    ): ResponseEntity<NotificationsResponse>

    @Operation(
        summary = "읽지 않은 알림 개수 조회",
        description = "현재 로그인한 사용자의 읽지 않은 알림 개수를 조회합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = UnreadCountResponse::class),
                    examples = [
                        ExampleObject(name = "UNREAD_COUNT", value = NotificationSwaggerResponseExample.UNREAD_COUNT)
                    ]
                )]
            ),
        ]
    )
    fun getUnreadCount(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
    ): ResponseEntity<UnreadCountResponse>

    @Operation(
        summary = "알림 읽음 처리",
        description = "알림 ID로 해당 알림을 읽음 상태로 변경합니다. 본인의 알림만 처리 가능합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "읽음 처리 성공"),
            ApiResponse(
                responseCode = "404",
                description = "알림을 찾을 수 없음",
                content = [Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ErrorResponse::class),
                    examples = [
                        ExampleObject(
                            name = "NOTIFICATION_NOT_FOUND",
                            value = NotificationSwaggerErrorExample.NotFound.NOTIFICATION
                        )
                    ]
                )]
            ),
        ]
    )
    fun markAsRead(
        @Parameter(description = "읽음 처리할 알림 ID", example = "1") id: NotificationId,
        @Parameter(description = "브로드캐스트 알림 여부", example = "false") isBroadcast: Boolean,
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
    ): ResponseEntity<Unit>

    @Operation(
        summary = "모든 알림 읽음 처리",
        description = "현재 로그인한 사용자의 모든 알림을 읽음 상태로 변경합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "전체 읽음 처리 성공"),
        ]
    )
    fun markAllAsRead(
        @Parameter(
            `in` = ParameterIn.COOKIE,
            name = "accessToken",
            description = "현재 로그인한 사용자 ID",
            schema = Schema(type = "string", example = "token_value")
        ) userId: CurrentUserId,
    ): ResponseEntity<Unit>
}

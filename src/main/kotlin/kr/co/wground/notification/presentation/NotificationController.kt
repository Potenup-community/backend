package kr.co.wground.notification.presentation

import kr.co.wground.global.common.NotificationId
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.notification.application.command.BroadcastNotificationCommandService
import kr.co.wground.notification.application.command.NotificationCommandService
import kr.co.wground.notification.application.query.NotificationQueryService
import kr.co.wground.notification.presentation.response.NotificationsResponse
import kr.co.wground.notification.presentation.response.UnreadCountResponse
import kr.co.wground.notification.presentation.response.toResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.data.web.SortDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationQueryService: NotificationQueryService,
    private val notificationCommandService: NotificationCommandService,
    private val broadcastNotificationCommandService: BroadcastNotificationCommandService,
) : NotificationApi {

    @GetMapping
    override fun getNotifications(
        @PageableDefault(size = 20)
        @SortDefault(sort = ["createdAt"], direction = Sort.Direction.DESC)
        pageable: Pageable,
        userId: CurrentUserId,
    ): ResponseEntity<NotificationsResponse> {
        val result = notificationQueryService.getNotifications(userId.value, pageable)
        return ResponseEntity.ok(result.toResponse())
    }

    @GetMapping("/unread-count")
    override fun getUnreadCount(userId: CurrentUserId): ResponseEntity<UnreadCountResponse> {
        val count = notificationQueryService.getUnreadCount(userId.value)
        return ResponseEntity.ok(UnreadCountResponse(count))
    }

    @PatchMapping("/{id}/read")
    override fun markAsRead(
        @PathVariable id: NotificationId,
        @RequestParam(defaultValue = "false") isBroadcast: Boolean,
        userId: CurrentUserId,
    ): ResponseEntity<Unit> {
        if (isBroadcast) {
            broadcastNotificationCommandService.markAsRead(userId.value, id)
        } else {
            notificationCommandService.markAsRead(id, userId.value)
        }
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/read-all")
    override fun markAllAsRead(userId: CurrentUserId): ResponseEntity<Unit> {
        notificationCommandService.markAllAsRead(userId.value)
        broadcastNotificationCommandService.markAllAsRead(userId.value)

        return ResponseEntity.noContent().build()
    }
}

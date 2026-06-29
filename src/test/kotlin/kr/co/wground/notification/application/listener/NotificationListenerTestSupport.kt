package kr.co.wground.notification.application.listener

import java.time.LocalDateTime
import kr.co.wground.notification.domain.enums.NotificationType
import kr.co.wground.notification.domain.vo.NotificationReference
import org.mockito.ArgumentCaptor
import org.mockito.Captor

abstract class NotificationListenerTestSupport {
    @Captor
    protected lateinit var recipientCaptor: ArgumentCaptor<Long>

    @Captor
    protected lateinit var actorCaptor: ArgumentCaptor<Long?>

    @Captor
    protected lateinit var typeCaptor: ArgumentCaptor<NotificationType>

    @Captor
    protected lateinit var titleCaptor: ArgumentCaptor<String>

    @Captor
    protected lateinit var referenceCaptor: ArgumentCaptor<NotificationReference?>

    @Captor
    protected lateinit var placeholdersCaptor: ArgumentCaptor<Map<String, String>>

    @Captor
    protected lateinit var expiresAtCaptor: ArgumentCaptor<LocalDateTime?>

    protected fun <T> capture(captor: ArgumentCaptor<T>): T = captor.capture()
}

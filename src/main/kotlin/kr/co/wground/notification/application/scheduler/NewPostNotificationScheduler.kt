package kr.co.wground.notification.application.scheduler

import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationMessageType
import kr.co.wground.notification.application.port.NotificationSender
import kr.co.wground.post.infra.PostRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class NewPostNotificationScheduler(
    private val postRepository: PostRepository,
    private val notificationSender: NotificationSender,
    @param:Value("\${app.frontend-url}") private val frontendUrl: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_DISPLAY_COUNT = 5
    }

    @Scheduled(cron = "0 0 * * * *")
    fun notifyNewPosts() {
        val cutoffTime = LocalDateTime.now().minusHours(1)
        val recentPosts = postRepository.findAllByCreatedAtAfterAndDeletedAtIsNull(cutoffTime)

        if (recentPosts.isEmpty()) {
            log.debug("No new posts in the last hour")
            return
        }

        val formattedPostList = recentPosts.take(MAX_DISPLAY_COUNT).mapIndexed { index, post ->
            "${index + 1}. ${post.postBody.title} (${post.topic.description})"
        }.joinToString("\n")

        val remainingPostsText = if (recentPosts.size > MAX_DISPLAY_COUNT) {
            "\n...외 ${recentPosts.size - MAX_DISPLAY_COUNT}개"
        } else ""

        notificationSender.send(
            NotificationMessage(
                type = NotificationMessageType.NEW_POSTS_SUMMARY,
                link = frontendUrl,
                metadata = mapOf(
                    "count" to recentPosts.size.toString(),
                    "posts" to formattedPostList + remainingPostsText,
                )
            )
        )

        log.info("Sent Slack notification for ${recentPosts.size} new posts")
    }
}

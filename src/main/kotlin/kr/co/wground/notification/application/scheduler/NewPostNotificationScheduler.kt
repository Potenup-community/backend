package kr.co.wground.notification.application.scheduler

import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationMessageType
import kr.co.wground.notification.application.port.NotificationSender
import kr.co.wground.notification.domain.enums.NotificationAudience
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.track.infra.TrackRepository
import kr.co.wground.user.infra.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class NewPostNotificationScheduler(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val trackRepository: TrackRepository,
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

        val writerIds = recentPosts.map { it.writerId }.distinct()
        val usersById = userRepository.findByUserIdIn(writerIds).associateBy { it.userId }
        val trackIds = usersById.values.map { it.trackId }.distinct()
        val tracksById = trackRepository.findAllById(trackIds).associateBy { it.trackId }

        val postsByAudience = recentPosts.groupBy { post ->
            val writer = usersById[post.writerId]
            val trackName = writer?.let { tracksById[it.trackId]?.displayName() }
            trackName?.let { NotificationAudience.fromTrackName(it) } ?: NotificationAudience.ALL
        }

        postsByAudience.forEach { (audience, posts) ->
            val formattedPostList = posts.take(MAX_DISPLAY_COUNT).mapIndexed { index, post ->
                "${index + 1}. ${post.postBody.title} (${post.topic.description})"
            }.joinToString("\n")

            val remainingPostsText = if (posts.size > MAX_DISPLAY_COUNT) {
                "\n...외 ${posts.size - MAX_DISPLAY_COUNT}개"
            } else ""

            notificationSender.send(
                NotificationMessage(
                    type = NotificationMessageType.NEW_POSTS_SUMMARY,
                    audience = audience,
                    link = frontendUrl,
                    metadata = mapOf(
                        "count" to posts.size.toString(),
                        "posts" to formattedPostList + remainingPostsText,
                    )
                )
            )
        }

        log.info("Sent Slack notification for ${recentPosts.size} new posts (groups=${postsByAudience.size})")
    }
}

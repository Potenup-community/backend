package kr.co.wground.reaction.application

import java.util.Optional
import kr.co.wground.comment.domain.Comment
import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.common.event.CommentReactionCreatedEvent
import kr.co.wground.common.event.PostReactionCreatedEvent
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.reaction.application.dto.CommentReactCommand
import kr.co.wground.reaction.application.dto.PostReactCommand
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.infra.jpa.CommentReactionJpaRepository
import kr.co.wground.reaction.infra.jpa.PostReactionJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationEventPublisher

class ReactionCommandServiceTest {
    private val postReactionJpaRepository = mock(PostReactionJpaRepository::class.java)
    private val commentReactionJpaRepository = mock(CommentReactionJpaRepository::class.java)
    private val postRepository = mock(PostRepository::class.java)
    private val commentRepository = mock(CommentRepository::class.java)
    private val eventPublisher = mock(ApplicationEventPublisher::class.java)
    private lateinit var reactionCommandService: ReactionCommandService

    @BeforeEach
    fun setUp() {
        reactionCommandService = ReactionCommandService(
            postReactionJpaRepository,
            commentReactionJpaRepository,
            postRepository,
            commentRepository,
            eventPublisher
        )
    }

    @DisplayName("게시글 좋아요 시 PostReactionCreatedEvent가 발행된다")
    @Test
    fun reactToPost_shouldPublishPostReactionCreatedEvent() {
        // given
        val postId = 1L
        val postWriterId = 100L
        val reactorId = 2L

        val post = Post.from(
            writerId = postWriterId,
            topic = Topic.KNOWLEDGE,
            title = "테스트 게시글",
            content = "테스트 본문",
            highlightType = HighlightType.NONE
        )

        val command = PostReactCommand(
            userId = reactorId,
            postId = postId,
            reactionType = ReactionType.LIKE
        )

        `when`(postRepository.findById(postId)).thenReturn(Optional.of(post))

        // when
        reactionCommandService.reactToPost(command)

        // then
        val captor = ArgumentCaptor.forClass(Any::class.java)
        verify(eventPublisher, times(2)).publishEvent(captor.capture())

        val events = captor.allValues
        val postReactionEvent = events.filterIsInstance<PostReactionCreatedEvent>().first()

        assertThat(postReactionEvent.postId).isEqualTo(postId)
        assertThat(postReactionEvent.postWriterId).isEqualTo(postWriterId)
        assertThat(postReactionEvent.reactorId).isEqualTo(reactorId)
    }

    @DisplayName("댓글 좋아요 시 CommentReactionCreatedEvent가 발행된다")
    @Test
    fun reactToComment_shouldPublishCommentReactionCreatedEvent() {
        // given
        val commentId = 10L
        val commentWriterId = 100L
        val reactorId = 2L

        val comment = Comment.create(commentWriterId, 1L, null, "테스트 댓글")
        setCommentId(comment, commentId)

        val command = CommentReactCommand(
            userId = reactorId,
            commentId = commentId,
            reactionType = ReactionType.LIKE
        )

        `when`(commentRepository.findById(commentId)).thenReturn(Optional.of(comment))

        // when
        reactionCommandService.reactToComment(command)

        // then
        val captor = ArgumentCaptor.forClass(CommentReactionCreatedEvent::class.java)
        verify(eventPublisher).publishEvent(captor.capture())

        val event = captor.value
        assertThat(event.commentId).isEqualTo(commentId)
        assertThat(event.commentWriterId).isEqualTo(commentWriterId)
        assertThat(event.reactorId).isEqualTo(reactorId)
    }

    private fun setCommentId(comment: Comment, id: Long) {
        val field = Comment::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(comment, id)
    }
}

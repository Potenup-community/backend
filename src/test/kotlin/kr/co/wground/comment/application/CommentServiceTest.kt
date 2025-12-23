package kr.co.wground.comment.application

import kr.co.wground.comment.domain.Comment
import kr.co.wground.comment.infra.CommentRepository
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.post.infra.PostRepository
import kr.co.wground.reaction.application.ReactionQueryService
import kr.co.wground.reaction.application.dto.CommentReactionStats
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.ArgumentMatchers.anySet
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl
import java.util.Optional
import kotlin.collections.emptyMap
import kotlin.jvm.java

class CommentServiceTest {
    private val commentRepository = mock(CommentRepository::class.java)
    private val postRepository = mock(PostRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)
    private val reactionQueryService = mock(ReactionQueryService::class.java)
    lateinit var commentService: CommentService

    @BeforeEach
    fun setUp() {
        commentService = CommentService(commentRepository, postRepository, userRepository, reactionQueryService)
    }

    @DisplayName("reactionStatsByCommentId가 비어있으면 emptyOf로 채운다")
    @Test
    fun getCommentsByPost_whenReactionStatsEmpty_shouldUseEmptyOfPerComment() {
        // given
        val postId = 1L
        val pageable = PageRequest.of(0, 10)
        val currentUserId = CurrentUserId(1L)


        `when`(postRepository.existsById(postId)).thenReturn(true)

        val parent1 = Comment.create(1L, postId, null, "p1")
        val parent2 = Comment.create(2L, postId, null, "p2")

        val user1 = User(
            userId = 1L,
            trackId = 100L,
            email = "user1@test.com",
            profileImageUrl = "https://cdn.test.com/user1.png",
            name = "유저1",
            phoneNumber = "010-1111-1111",
            provider = "KAKAO",
            role = UserRole.ADMIN,
            status = UserStatus.ACTIVE
        )

        val user2 = User(
            userId = 2L,
            trackId = 100L,
            email = "user2@test.com",
            profileImageUrl = "https://cdn.test.com/user2.png",
            name = "유저2",
            phoneNumber = "010-2222-2222",
            provider = "KAKAO",
            role = UserRole.ADMIN,
            status = UserStatus.ACTIVE
        )

        val post = Post.from(
            writerId = 1L,
            topic = Topic.KNOWLEDGE,
            title = "테스트 게시글 제목",
            content = "테스트 게시글 본문입니다.",
            highlightType = HighlightType.NONE
        )

        `when`(commentRepository.findByPostIdAndParentIdIsNull(postId, pageable))
            .thenReturn(SliceImpl(listOf(parent1, parent2), pageable, false))

        `when`(commentRepository.findByPostIdAndParentIdIn(postId, listOf(parent1.id, parent2.id)))
            .thenReturn(emptyList())

        `when`(postRepository.findById(postId))
            .thenReturn(Optional.of(post))

        `when`(userRepository.findAllById(anyList()))
            .thenReturn(listOf(user1, user2))

        // reaction stats empty
        `when`(reactionQueryService.getCommentReactionStats(anySet(), eq(currentUserId.value)))
            .thenReturn(emptyMap())

        // when
        val result = commentService.getCommentsByPost(postId, pageable, currentUserId)

        // then
        assertThat(result.content).hasSize(2)
        result.content.forEach { dto ->
            assertThat(dto.commentReactionStats)
                .isEqualTo(CommentReactionStats.emptyOf(dto.commentId))
        }
    }
}

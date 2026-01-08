package kr.co.wground.comment.application


import java.util.Optional
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
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anySet
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.SliceImpl

class CommentServiceTest {
    private val commentRepository = mock(CommentRepository::class.java)
    private val postRepository = mock(PostRepository::class.java)
    private val userRepository = mock(UserRepository::class.java)
    private val reactionQueryService = mock(ReactionQueryService::class.java)
    private lateinit var commentService: CommentService

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

        val parent1 = Comment.create(1L, postId, null, "p1")
        val parent2 = Comment.create(2L, postId, null, "p2")

        val user1 = User(
            userId = 1L,
            trackId = 100L,
            email = "user1@test.com",
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

        `when`(commentRepository.findAllByPostId(postId))
            .thenReturn(mutableListOf(parent1, parent2))

        `when`(commentRepository.findByPostIdAndParentIdIsNull(postId, pageable))
            .thenReturn(SliceImpl(listOf(parent1, parent2), pageable, false))

        `when`(commentRepository.findByPostIdAndParentIdIn(postId, listOf(parent1.id, parent2.id)))
            .thenReturn(emptyList())

        `when`(postRepository.findById(postId))
            .thenReturn(Optional.of(post))

        `when`(userRepository.findAllById(anyList()))
            .thenReturn(listOf(user1, user2))

        `when`(reactionQueryService.getCommentReactionStats(anySet(), eq(currentUserId.value)))
            .thenReturn(emptyMap())

        // when
        val result = commentService.getCommentsByPost(postId, currentUserId)

        // then
        assertThat(result).hasSize(2)
        result.forEach { dto ->
            assertThat(dto.commentReactionStats)
                .isEqualTo(CommentReactionStats.emptyOf(dto.commentId))
        }
    }

    @DisplayName("전체 댓글 조회 시 여러 유저의 트랙 이름이 조회된다.")
    @Test
    fun getCommentsByPost_whenTrackNamesByUserIds_shouldLoadTrackName_() {
        // given
        givenPostExists(1L)
        givenComments(
            listOf(
                comment(1L, 10L, 1L),
                comment(2L, 20L, 1L)
            )
        )
        givenUserTrackNames(
            mapOf(10L to "트랙A", 20L to "트랙B")
        )
        givenEmptyReactionStats(1L)

        // when
        val result = commentService.getCommentsByPost(1L, CurrentUserId(1L))

        // then
        assertThat(result.first().trackName).isEqualTo("트랙A")
    }

    private fun givenPostExists(postId: Long) {
        val post = Post.from(
            writerId = 1L,
            topic = Topic.KNOWLEDGE,
            title = "title",
            content = "content",
            highlightType = HighlightType.NONE
        )
        `when`(postRepository.findById(postId))
            .thenReturn(Optional.of(post))
    }

    private fun givenComments(comments: List<Comment>) {
        `when`(commentRepository.findAllByPostId(anyLong()))
            .thenReturn(comments as MutableList<Comment>?)

        val parentIds = comments
            .filter { it.parentId == null }
            .map { it.id }

        `when`(commentRepository.findByPostIdAndParentIdIn(anyLong(), anyList()))
            .thenReturn(comments.filter { it.parentId != null })
    }

    private fun givenUserTrackNames(userIdToTrack: Map<Long, String>) {
        val users = userIdToTrack.keys.map {
            User(
                userId = it,
                trackId = it * 10,
                email = "$it@test.com",
                name = "user$it",
                phoneNumber = "010-0000-0000",
                provider = "GOOGLE",
                role = UserRole.ADMIN,
                status = UserStatus.ACTIVE
            )
        }

        `when`(userRepository.findAllById(anySet()))
            .thenReturn(users)

        `when`(userRepository.findUserAndTrackName(anyList()))
            .thenReturn(userIdToTrack)
    }

    private fun givenEmptyReactionStats(currentUserId: Long) {
        `when`(
            reactionQueryService.getCommentReactionStats(anySet(), eq(currentUserId))
        ).thenReturn(emptyMap())
    }

    private fun comment(
        id: Long,
        writerId: Long,
        postId: Long,
        parentId: Long? = null
    ): Comment =
        Comment.create(writerId, postId, parentId, "content")
            .apply { setTestId(id) }

    private fun Comment.setTestId(id: Long) {
        val field = Comment::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(this, id)
    }
}

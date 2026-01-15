package kr.co.wground.comment.application

import java.time.LocalDateTime
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
import kr.co.wground.reaction.application.dto.LikedCommentDto
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import kr.co.wground.user.infra.dto.UserDisplayInfoDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
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

    @DisplayName("내가 좋아요한 댓글을 조회한다 (삭제 댓글은 [삭제된 댓글]로 반환)")
    @Test
    fun getLikedComments_whenLikedCommentsExist_shouldReturnSummaries() {
        // given
        val currentUserId = CurrentUserId(1L)
        val pageable = PageRequest.of(0, 2)

        val likedAt1 = LocalDateTime.now().minusDays(1)
        val likedAt2 = LocalDateTime.now()

        val liked = SliceImpl(
            listOf(
                LikedCommentDto(commentId = 10L, likedAt = likedAt1),
                LikedCommentDto(commentId = 20L, likedAt = likedAt2),
            ),
            pageable,
            false
        )

        val comment1 = Comment.create(2L, 100L, null, "hello")
        setCommentId(comment1, 10L)

        val comment2 = Comment.create(3L, 200L, null, "will be deleted")
        setCommentId(comment2, 20L)
        comment2.deleteContent()

        `when`(reactionQueryService.getLikedComments(currentUserId.value, pageable))
            .thenReturn(liked)
        `when`(commentRepository.findAllById(setOf(10L, 20L)))
            .thenReturn(listOf(comment1, comment2))
        `when`(userRepository.findUserDisplayInfos(anyList()))
            .thenReturn(
                mapOf(
                    2L to UserDisplayInfoDto(userId = 2L, name = "유저2", profileImageUrl = "p2", trackName = "트랙2"),
                    3L to UserDisplayInfoDto(userId = 3L, name = "유저3", profileImageUrl = "p3", trackName = "트랙3"),
                )
            )
        `when`(reactionQueryService.getCommentReactionStats(setOf(10L, 20L), currentUserId.value))
            .thenReturn(emptyMap())

        // when
        val result = commentService.getLikedComments(currentUserId, pageable)

        // then
        assertThat(result.content).hasSize(2)
        assertThat(result.content[0].commentId).isEqualTo(10L)
        assertThat(result.content[0].postId).isEqualTo(100L)
        assertThat(result.content[0].likedAt).isEqualTo(likedAt1)

        assertThat(result.content[1].commentId).isEqualTo(20L)
        assertThat(result.content[1].postId).isEqualTo(200L)
        assertThat(result.content[1].content).isEqualTo("[삭제된 댓글]")
        assertThat(result.content[1].likedAt).isEqualTo(likedAt2)
    }

    @DisplayName("좋아요한 댓글이 없으면 빈 리스트를 반환한다")
    @Test
    fun getLikedComments_whenEmpty_shouldReturnEmptySlice() {
        // given
        val currentUserId = CurrentUserId(1L)
        val pageable = PageRequest.of(0, 20)

        `when`(reactionQueryService.getLikedComments(currentUserId.value, pageable))
            .thenReturn(SliceImpl(emptyList(), pageable, false))

        // when
        val result = commentService.getLikedComments(currentUserId, pageable)

        // then
        assertThat(result.content).isEmpty()
        assertThat(result.hasNext()).isFalse()
    }

    @DisplayName("탈퇴한 유저의 댓글은 기본값으로 반환된다")
    @Test
    fun getLikedComments_whenAuthorNotFound_shouldUseDefaultValues() {
        // given
        val currentUserId = CurrentUserId(1L)
        val pageable = PageRequest.of(0, 10)
        val likedAt = LocalDateTime.now()

        val liked = SliceImpl(
            listOf(LikedCommentDto(commentId = 10L, likedAt = likedAt)),
            pageable,
            false
        )

        val comment = Comment.create(999L, 100L, null, "탈퇴한 유저의 댓글")
        setCommentId(comment, 10L)

        `when`(reactionQueryService.getLikedComments(currentUserId.value, pageable))
            .thenReturn(liked)
        `when`(commentRepository.findAllById(setOf(10L)))
            .thenReturn(listOf(comment))
        `when`(userRepository.findUserDisplayInfos(anyList()))
            .thenReturn(emptyMap())
        `when`(reactionQueryService.getCommentReactionStats(setOf(10L), currentUserId.value))
            .thenReturn(emptyMap())

        // when
        val result = commentService.getLikedComments(currentUserId, pageable)

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].authorName).isEqualTo("탈퇴한 사용자")
        assertThat(result.content[0].trackName).isEqualTo("소속 없음")
    }

    @DisplayName("다음 페이지가 있으면 hasNext가 true다")
    @Test
    fun getLikedComments_whenHasNextPage_shouldReturnHasNextTrue() {
        // given
        val currentUserId = CurrentUserId(1L)
        val pageable = PageRequest.of(0, 1)
        val likedAt = LocalDateTime.now()

        val liked = SliceImpl(
            listOf(LikedCommentDto(commentId = 10L, likedAt = likedAt)),
            pageable,
            true
        )

        val comment = Comment.create(2L, 100L, null, "댓글")
        setCommentId(comment, 10L)

        `when`(reactionQueryService.getLikedComments(currentUserId.value, pageable))
            .thenReturn(liked)
        `when`(commentRepository.findAllById(setOf(10L)))
            .thenReturn(listOf(comment))
        `when`(userRepository.findUserDisplayInfos(anyList()))
            .thenReturn(
                mapOf(2L to UserDisplayInfoDto(userId = 2L, name = "유저2", profileImageUrl = "p2", trackName = "트랙2"))
            )
        `when`(reactionQueryService.getCommentReactionStats(setOf(10L), currentUserId.value))
            .thenReturn(emptyMap())

        // when
        val result = commentService.getLikedComments(currentUserId, pageable)

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.hasNext()).isTrue()
    }

    @DisplayName("좋아요한 댓글이 DB에서 삭제되었으면 결과에서 제외된다")
    @Test
    fun getLikedComments_whenCommentNotFoundInDb_shouldSkipIt() {
        // given
        val currentUserId = CurrentUserId(1L)
        val pageable = PageRequest.of(0, 10)
        val likedAt1 = LocalDateTime.now().minusDays(1)
        val likedAt2 = LocalDateTime.now()

        val liked = SliceImpl(
            listOf(
                LikedCommentDto(commentId = 10L, likedAt = likedAt1),
                LikedCommentDto(commentId = 20L, likedAt = likedAt2),
            ),
            pageable,
            false
        )

        val comment1 = Comment.create(2L, 100L, null, "존재하는 댓글")
        setCommentId(comment1, 10L)

        `when`(reactionQueryService.getLikedComments(currentUserId.value, pageable))
            .thenReturn(liked)
        `when`(commentRepository.findAllById(setOf(10L, 20L)))
            .thenReturn(listOf(comment1))
        `when`(userRepository.findUserDisplayInfos(anyList()))
            .thenReturn(
                mapOf(2L to UserDisplayInfoDto(userId = 2L, name = "유저2", profileImageUrl = "p2", trackName = "트랙2"))
            )
        `when`(reactionQueryService.getCommentReactionStats(setOf(10L), currentUserId.value))
            .thenReturn(emptyMap())

        // when
        val result = commentService.getLikedComments(currentUserId, pageable)

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].commentId).isEqualTo(10L)
    }

    private fun setCommentId(comment: Comment, id: Long) {
        val field = Comment::class.java.getDeclaredField("id")
        field.isAccessible = true
        field.set(comment, id)
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

        `when`(userRepository.findUserDisplayInfos(anyList()))
            .thenReturn(
                mapOf(
                    user1.userId to UserDisplayInfoDto(
                        userId = user1.userId,
                        name = user1.name,
                        profileImageUrl = user1.accessProfile(),
                        trackName = "트랙",
                    ),
                    user2.userId to UserDisplayInfoDto(
                        userId = user2.userId,
                        name = user2.name,
                        profileImageUrl = user2.accessProfile(),
                        trackName = "트랙",
                    ),
                )
            )

        `when`(reactionQueryService.getCommentReactionStats(setOf(parent1.id, parent2.id), currentUserId.value))
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
    fun getCommentsByPost_whenTrackNamesByUserIds_shouldLoadTrackName() {
        // given
        val postId = 1L
        val currentUserId = CurrentUserId(1L)

        val comment1 = Comment.create(1L, postId, null, "p1")
        val comment2 = Comment.create(2L, postId, null, "p2")

        val post = Post.from(
            writerId = 1L,
            topic = Topic.KNOWLEDGE,
            title = "테스트 게시글 제목",
            content = "테스트 게시글 본문입니다.",
            highlightType = HighlightType.NONE
        )

        `when`(postRepository.findById(postId)).thenReturn(Optional.of(post))
        `when`(commentRepository.findAllByPostId(postId)).thenReturn(
            listOf(
                comment1,
                comment2
            ) as MutableList<Comment>?
        )

        `when`(userRepository.findUserDisplayInfos(anyList()))
            .thenReturn(
                mapOf(
                    1L to UserDisplayInfoDto(
                        userId = 1L,
                        name = "유저1",
                        profileImageUrl = "profile-1",
                        trackName = "트랙1",
                    ),
                    2L to UserDisplayInfoDto(
                        userId = 2L,
                        name = "유저2",
                        profileImageUrl = "profile-2",
                        trackName = "트랙2",
                    ),
                )
            )

        `when`(reactionQueryService.getCommentReactionStats(setOf(comment1.id, comment2.id), currentUserId.value))
            .thenReturn(emptyMap())

        // when
        val result = commentService.getCommentsByPost(postId, currentUserId)

        // then
        assertThat(result).hasSize(2)
        val trackNameByAuthorId = result.associate { it.authorId to it.trackName }
        assertThat(trackNameByAuthorId[1L]).isEqualTo("트랙1")
        assertThat(trackNameByAuthorId[2L]).isEqualTo("트랙2")
    }


}

package kr.co.wground.reaction.infra.jpa

import kr.co.wground.reaction.domain.PostReaction
import kr.co.wground.global.common.PostId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.infra.querydsl.CustomPostReactionRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface PostReactionJpaRepository : JpaRepository<PostReaction, Long>, CustomPostReactionRepository {

    fun deleteByUserIdAndPostIdAndReactionType(userId: UserId, postId: PostId, reactionType: ReactionType): Long

    /**
     * MySQL(H2 for test) 의 upsert 문법을 활용한 멱등 insert
     * 예외가 발생하지 않으면 정상 동작한 것으로 간주합니다.
     */
    @Modifying(clearAutomatically = true)
    @Query(
        nativeQuery = true,
        value = "INSERT INTO post_reaction (user_id, post_id, reaction_type, created_at)" +
                "VALUES (:userId, :postId, :reactionType, :now)" +
                "ON DUPLICATE KEY UPDATE id = id" // 기본 키 중복 시 사실 상 no-op
    )
    fun saveIdempotentlyForMysqlOrH2(
        @Param("userId") userId: UserId,
        @Param("postId") postId: PostId,
        @Param("reactionType") reactionType: ReactionType,
        @Param("now") now: LocalDateTime
    )

    fun findPostReactionsByPostId(postId: Long): List<PostReaction>
}

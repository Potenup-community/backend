package kr.co.wground.reaction.infra.jpa

import kr.co.wground.global.common.CommentId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.CommentReaction
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.infra.querydsl.CustomCommentReactionRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface CommentReactionJpaRepository : JpaRepository<CommentReaction, Long>, CustomCommentReactionRepository {
    fun deleteByUserIdAndCommentIdAndReactionType(userId: UserId, commentId: CommentId, reactionType: ReactionType) : Long

    /**
     * MySQL(H2 for test) 의 upsert 문법을 활용한 멱등 insert
     * 예외가 발생하지 않으면 정상 동작한 것으로 간주합니다.
     */
    @Modifying(clearAutomatically = true)
    @Query(
        nativeQuery = true,
        value = "INSERT INTO comment_reaction (user_id, comment_id, reaction_type, created_at)" +
                "VALUES (:userId, :commentId, :reactionType, :now)" +
                "ON DUPLICATE KEY UPDATE id = id" // 기본 키 중복 시 사실 상 no-op
    )
    fun saveIdempotentlyForMysqlOrH2(
        @Param("userId") userId: UserId,
        @Param("commentId") commentId: CommentId,
        @Param("reactionType") reactionType: ReactionType,
        @Param("now") now: LocalDateTime
    )
}

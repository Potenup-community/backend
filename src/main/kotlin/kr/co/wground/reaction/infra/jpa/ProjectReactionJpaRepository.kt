package kr.co.wground.reaction.infra.jpa

import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import kr.co.wground.reaction.domain.ProjectReaction
import kr.co.wground.reaction.domain.enums.ReactionType
import kr.co.wground.reaction.infra.querydsl.CustomProjectReactionRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ProjectReactionJpaRepository : JpaRepository<ProjectReaction, Long>, CustomProjectReactionRepository {

    fun deleteByUserIdAndProjectIdAndReactionType(
        userId: UserId,
        projectId: ProjectId,
        reactionType: ReactionType,
    ): Long

    @Modifying(clearAutomatically = true)
    @Query(
        nativeQuery = true,
        value = "INSERT INTO project_reaction (user_id, project_id, reaction_type, created_at) " +
                "VALUES (:userId, :projectId, :reactionType, :now) " +
                "ON DUPLICATE KEY UPDATE id = id"
    )
    fun saveIdempotentlyForMysqlOrH2(
        @Param("userId") userId: UserId,
        @Param("projectId") projectId: ProjectId,
        @Param("reactionType") reactionType: ReactionType,
        @Param("now") now: LocalDateTime,
    )
}

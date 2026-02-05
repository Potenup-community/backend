package kr.co.wground.study.infra

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.StudyRecruitment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface StudyRecruitmentRepository : JpaRepository<StudyRecruitment, Long> {
    fun existsByStudyIdAndUserId(studyId: Long, userId: Long): Boolean

    @Query(
        """
             SELECT COUNT(sr) FROM StudyRecruitment sr
             JOIN sr.study s
             WHERE sr.userId = :userId
               AND s.scheduleId = :scheduleId
         """
    )
    fun countStudyRecruitment(
        userId: Long,
        scheduleId: Long,
    ): Long

    @Query(
        """
             SELECT r.study.id
            FROM StudyRecruitment r
             WHERE r.userId = :userId
              AND r.study.id IN :studyIds
        """
    )
    fun findAllByUserIdAndStudyIds(
        @Param("userId") userId: Long,
        @Param("studyIds") studyIds: List<Long>
    ): List<Long>

    fun findAllByUserId(userId: UserId): List<StudyRecruitment>

    fun findAllByStudyId(studyId: Long): List<StudyRecruitment>
}
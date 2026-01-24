package kr.co.wground.study.infra

import java.time.LocalDateTime
import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.StudyRecruitment
import kr.co.wground.study.domain.constant.RecruitStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface StudyRecruitmentRepository : JpaRepository<StudyRecruitment, Long> {
    fun findAllByStudyIdAndRecruitStatus(studyId: Long, status: RecruitStatus): List<StudyRecruitment>
    fun existsByStudyIdAndUserIdAndRecruitStatusIn(studyId: Long, userId: Long, status: List<RecruitStatus>): Boolean

    @Query(
        """
             SELECT COUNT(sr) FROM StudyRecruitment sr
             JOIN sr.study s
             WHERE sr.userId = :userId
               AND s.scheduleId = :scheduleId
               AND sr.recruitStatus NOT IN :excludedStatuses
         """
    )
    fun countActiveEnrolledStudy(
        userId: Long,
        scheduleId: Long,
        excludedStatuses: List<RecruitStatus> = listOf(
            RecruitStatus.CANCELLED,
            RecruitStatus.REJECTED
        )
    ): Long

    @Modifying
    @Query(
        """
        UPDATE StudyRecruitment sr 
        SET sr.recruitStatus = :status, sr.updatedAt = :now 
        WHERE sr.study.id = :studyId 
          AND sr.recruitStatus != :targetStatus
    """
    )
    fun rejectAllByStudyIdWithExceptStatus(
        studyId: Long,
        targetStatus: RecruitStatus,
        status: RecruitStatus = RecruitStatus.REJECTED,
        now: LocalDateTime = LocalDateTime.now()
    ): Int

    fun findAllByUserId(userId: UserId): List<StudyRecruitment>

    fun findAllByStudyId(studyId: Long): List<StudyRecruitment>
}
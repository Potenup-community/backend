package kr.co.wground.study.infra

import kr.co.wground.study.domain.StudyRecruitment
import kr.co.wground.study.domain.constant.RecruitStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface StudyRecruitmentRepository : JpaRepository<StudyRecruitment, Long> {
    fun findAllByStudyIdAndRecruitStatus(studyId: Long, status: RecruitStatus): List<StudyRecruitment>
    fun existsByStudyIdAndUserIdAndRecruitStatus(studyId: Long, userId: Long, status: RecruitStatus): Boolean

    @Query(
        """
             SELECT COUNT(sr) FROM StudyRecruitment sr
             JOIN sr.study s
             WHERE sr.userId = :userId
               AND s.scheduleId = :scheduleId
               AND sr.recruitStatus NOT IN :excludedStatuses
         """
    )
    fun countActiveEnrolledStudy(userId: Long, scheduleId: Long, excludedStatuses: List<RecruitStatus> = listOf(RecruitStatus.CANCELLED, RecruitStatus.REJECTED)): Long
}
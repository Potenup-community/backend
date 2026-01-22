package kr.co.wground.study.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.constant.Months
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals

@DisplayName("스터디 일정(StudySchedule) 테스트")
class StudyScheduleTest {

    @Nested
    @DisplayName("내부 요구조건 테스트")
    inner class StudyScheduleInternalConditionTest {

        @Test
        fun `일정 생성 시, 모집 시작 시점이 모집 종료 시점 이후인 경우, 예외 발생 - BusinessException(SS-0001)`() {
            val thrown = assertThrows<BusinessException> {
                val created = StudySchedule(
                    trackId = 3L,
                    months = Months.THIRD,
                    recruitStartDate = LocalDate.now(),
                    recruitEndDate = LocalDate.now().minusDays(1),
                    studyEndDate = LocalDate.now().plusDays(1)
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_CANT_START_AFTER_END_DATE.code, thrown.code)
        }

        @Test
        fun `일정 수정 시, 모집 시작 시점이 모집 종료 시점 이후인 경우, 예외 발생 - BusinessException(SS-0001)`() {
            val thrown = assertThrows<BusinessException> {
                val created = StudySchedule(
                    trackId = 3L,
                    months = Months.THIRD,
                    recruitStartDate = LocalDate.now(),
                    recruitEndDate = LocalDate.now().plusDays(1),
                    studyEndDate = LocalDate.now().plusDays(2)
                )

                created.updateSchedule(
                    newRecruitStart = LocalDate.now(),
                    newRecruitEnd = LocalDate.now().minusDays(1),
                    newStudyEnd = LocalDate.now().plusDays(1)
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_CANT_START_AFTER_END_DATE.code, thrown.code)
        }

        @Test
        fun `일정 생성 시, 모집 종료 시점이 스터디 종료 시점 이후인 경우, 예외 발생 - BusinessException(SS-0002)`() {
            val thrown = assertThrows<BusinessException> {
                val created = StudySchedule(
                    trackId = 3L,
                    months = Months.THIRD,
                    recruitStartDate = LocalDate.now(),
                    recruitEndDate = LocalDate.now().plusDays(2),
                    studyEndDate = LocalDate.now().plusDays(1)
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE.code, thrown.code)
        }

        @Test
        fun `일정 수정 시, 모집 종료 시점이 스터디 종료 시점 이후인 경우, 예외 발생 - BusinessException(SS-0002)`() {
            val thrown = assertThrows<BusinessException> {
                val created = StudySchedule(
                    trackId = 3L,
                    months = Months.THIRD,
                    recruitStartDate = LocalDate.now(),
                    recruitEndDate = LocalDate.now().plusDays(1),
                    studyEndDate = LocalDate.now().plusDays(2)
                )

                created.updateSchedule(
                    newRecruitStart = LocalDate.now(),
                    newRecruitEnd = LocalDate.now().plusDays(2),
                    newStudyEnd = LocalDate.now().plusDays(1)
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_RECRUIT_COMPLETE_BEFORE_END_DATE.code, thrown.code)
        }
    }
}
package kr.co.wground.study.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.constant.BudgetType
import kr.co.wground.study.domain.constant.Months
import kr.co.wground.study.domain.constant.RecruitStatus
import kr.co.wground.study.domain.constant.StudyStatus
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class StudyRecruitmentTest {

    // ----- 자기 소개, ⚠️ SR-0001 에 해당하는 에러 코드가 RECRUITMENT_APPEAL_INVALID 와 같이 변경됨을 가정함️

    @Test
    fun `자기소개 생성 시, 앞뒤 공백 제거 기준, 자기소개가 blank 인 경우 예외 발생 - BusinessException(SR-0001)`() {
        val thrown = assertThrows<BusinessException> {
            createStudyRecruitmentWithAppeal(
                createRecruitingStudySchedule(), "  \t  \n  ")
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_APPEAL_EMPTY.code, thrown.code)
    }

    @Test
    fun `자기소개 생성 시, 앞뒤 공백 제거 기준, 자기소개 길이가 2 미만인 경우, 예외 발생 - BusinessException(SR-0001)`() {
        val thrown = assertThrows<BusinessException> {
            createStudyRecruitmentWithAppeal(
                createRecruitingStudySchedule(), "  \t A \n  ")
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_APPEAL_INVALID_LENGTH_RANGE.code, thrown.code)
    }

    @Test
    fun `자기소개 생성 시, 앞뒤 공백 제거 기준, 자기소개 길이가 MAX_APPEAL_LENGTH 초과인 경우, 예외 발생 - BusinessException(SR-0001)`() {
        val thrown = assertThrows<BusinessException> {
            createStudyRecruitmentWithAppeal(
                createRecruitingStudySchedule(),
                "*".repeat(StudyRecruitment.MAX_APPEAL_LENGTH) + 1
            )
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_APPEAL_INVALID_LENGTH_RANGE.code, thrown.code)
    }

    @Test
    fun `자기소개 수정 시, 앞뒤 공백 제거 기준, 자기소개가 blank 인 경우 예외 발생 - BusinessException(SR-0001)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(
                createRecruitingStudySchedule(), "유효한 자기 소개")

            created.updateAppeal("  \t \n  ")
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_APPEAL_EMPTY.code, thrown.code)
    }

    @Test
    fun `자기소개 수정 시, 앞뒤 공백 제거 기준, 자기소개 길이가 2 미만인 경우, 예외 발생 - BusinessException(SR-0001)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(
                createRecruitingStudySchedule(), "유효한 자기소개")

            created.updateAppeal("1")
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_APPEAL_INVALID_LENGTH_RANGE.code, thrown.code)
    }

    @Test
    fun `자기소개 수정 시, 앞뒤 공백 제거 기준, 자기소개 길이가 MAX_APPEAL_LENGTH 초과인 경우, 예외 발생 - BusinessException(SR-0001)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(
                createRecruitingStudySchedule(), "유효한 자기소개")

            created.updateAppeal("*".repeat(StudyRecruitment.MAX_APPEAL_LENGTH + 1))
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_APPEAL_INVALID_LENGTH_RANGE .code, thrown.code)
    }

    // ----- 승인

    @Test
    fun `신청이 승인된 경우, 승인 일시가 null 이거나 신청 일시 이전이면 안 된다`() {
        val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "예시 자기 소개")
        Thread.sleep(10)
        created.updateRecruitStatus(RecruitStatus.APPROVED)

        assertAll(
            { assertEquals(RecruitStatus.APPROVED, created.recruitStatus) },
            { assertNotNull(created.updatedAt) },
            { assertTrue { created.createdAt.isBefore(created.updatedAt) || created.createdAt.isEqual(created.updatedAt) } }
        )
    }

    @Test
    fun `신청이 CANCELLED 상태일 때, 승인을 시도하면, 예외 발생 - BusinessException(SR-0003)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.CANCELLED)

            created.updateRecruitStatus(RecruitStatus.APPROVED)
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    @Test
    fun `신청이 APPROVED 상태일 때, 승인을 시도하면, 예외 발생 - BusinessException(SR-0003)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.APPROVED)

            created.updateRecruitStatus(RecruitStatus.APPROVED)
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    @Test
    fun `신청이 REJECTED 상태일 때, 승인을 시도하면, 예외 발생 - BusinessException(SR-0003)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.REJECTED)

            created.updateRecruitStatus(RecruitStatus.APPROVED)
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }
    
    // ----- 취소

    @Test
    fun `신청이 CANCELLED 상태일 때, 취소를 시도하면, 예외 발생 - BusinessException(SR-0003)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.CANCELLED)

            created.updateRecruitStatus(RecruitStatus.CANCELLED)
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    @Test
    fun `신청이 REJECTED 상태일 때, 취소를 시도하면, 예외 발생 - BusinessException(SR-0003)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.REJECTED)

            created.updateRecruitStatus(RecruitStatus.CANCELLED)
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    // ----- 반려

    @Test
    fun `신청이 CANCELLED 상태일 때, 반려를 시도하면, 예외 발생 - BusinessException(SR-0003)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.CANCELLED)

            created.updateRecruitStatus(RecruitStatus.REJECTED)
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    @Test
    fun `신청이 APPROVED 상태일 때, 반려를 시도하면, 예외 발생 - BusinessException(SR-0003)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.APPROVED)

            created.updateRecruitStatus(RecruitStatus.REJECTED)
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    @Test
    fun `신청이 REJECTED 상태일 때, 반려를 시도하면, 예외 발생 - BusinessException(SR-0003)`() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.REJECTED)

            created.updateRecruitStatus(RecruitStatus.REJECTED)
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    // factories

    private fun createRecruitingStudySchedule(): StudySchedule {
        return StudySchedule(
            trackId = 3L,
            months = Months.FIRST,
            recruitStartDate = LocalDate.now().minusDays(4),
            recruitEndDate = LocalDate.now().plusDays(3),
            studyEndDate = LocalDate.now().plusDays(26)
        )
    }

    private fun createSampleStudy(schedule: StudySchedule): Study {
        return Study(
            budget = BudgetType.BOOK,
            name = "스터디 제목",
            description = "스터디 소개글",
            leaderId = 1L,
            trackId = 3L,
            scheduleId = schedule.id,
            status = StudyStatus.PENDING
        )
    }

    private fun createStudyRecruitmentWithAppeal(schedule: StudySchedule, appeal: String): StudyRecruitment {
        return StudyRecruitment.apply(
            userId = 2L,
            study = createSampleStudy(schedule),
            appeal = appeal
        )
    }
}
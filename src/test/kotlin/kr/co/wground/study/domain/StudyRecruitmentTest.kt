package kr.co.wground.study.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.constant.BudgetType
import kr.co.wground.study.domain.constant.Months
import kr.co.wground.study.domain.constant.RecruitStatus
import kr.co.wground.study.domain.constant.StudyStatus
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.LocalDate
import java.util.stream.Stream

@DisplayName("스터디 신청(StudyRecruitment) 테스트")
class StudyRecruitmentTest {

    companion object {
        @JvmStatic
        fun invalidAppeals(): Stream<Arguments> = Stream.of(
            Arguments.of("empty", "", StudyDomainErrorCode.RECRUITMENT_APPEAL_EMPTY.code),
            Arguments.of("blank(space)", " ", StudyDomainErrorCode.RECRUITMENT_APPEAL_EMPTY.code),
            Arguments.of("blank(tab)", "\t", StudyDomainErrorCode.RECRUITMENT_APPEAL_EMPTY.code),
            Arguments.of("blank(newline)", "\n", StudyDomainErrorCode.RECRUITMENT_APPEAL_EMPTY.code),
            Arguments.of("blank(mixed)", " \t \n ", StudyDomainErrorCode.RECRUITMENT_APPEAL_EMPTY.code),
            Arguments.of("too short(${StudyRecruitment.MIN_APPEAL_LENGTH}자 미만)", "A".repeat(StudyRecruitment.MIN_APPEAL_LENGTH - 1), StudyDomainErrorCode.RECRUITMENT_APPEAL_INVALID_LENGTH_RANGE.code),
            Arguments.of("too short(${StudyRecruitment.MIN_APPEAL_LENGTH}}자 미만; trimmed)", " A ", StudyDomainErrorCode.RECRUITMENT_APPEAL_INVALID_LENGTH_RANGE.code),
            Arguments.of("too long(${StudyRecruitment.MAX_APPEAL_LENGTH}자 초과)", " \t" + "A".repeat(StudyRecruitment.MAX_APPEAL_LENGTH + 1) + "\n ", StudyDomainErrorCode.RECRUITMENT_APPEAL_INVALID_LENGTH_RANGE.code)
        )

        @JvmStatic
        fun recruitmentStatusesCannotBeApproved(): Stream<Arguments> = Stream.of(
            Arguments.of("APPROVED", RecruitStatus.APPROVED, StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code),
            Arguments.of("CANCELLED", RecruitStatus.CANCELLED, StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code),
            Arguments.of("REJECTED", RecruitStatus.REJECTED, StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code),
        )

        @JvmStatic
        fun recruitmentStatusesCannotBeCancelled(): Stream<Arguments> = Stream.of(
            Arguments.of("CANCELLED", RecruitStatus.CANCELLED, StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code),
            Arguments.of("REJECTED", RecruitStatus.REJECTED, StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code),
        )

        @JvmStatic
        fun recruitmentStatusesCannotBeRejected(): Stream<Arguments> = Stream.of(
            Arguments.of("APPROVED", RecruitStatus.APPROVED, StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code),
            Arguments.of("CANCELLED", RecruitStatus.CANCELLED, StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code),
            Arguments.of("REJECTED", RecruitStatus.REJECTED, StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code),
        )
    }

    // ----- 자기 소개, ⚠️ SR-0001 에 해당하는 에러 코드가 RECRUITMENT_APPEAL_INVALID 와 같이 변경됨을 가정함️

    @ParameterizedTest(name = "자기소개: {0}")
    @MethodSource("invalidAppeals")
    @DisplayName("자기소개 생성 시, 앞뒤 공백 제거 기준, 자기소개가 유효하지 않은 경우 예외 발생")
    fun shouldThrowRecruitmentAppealEmpty_whenCreateWithInvalidAppeal(caseName: String, givenAppeal: String, expectedErrorCode: String) {
        val thrown = assertThrows<BusinessException> {
            createStudyRecruitmentWithAppeal(
                createRecruitingStudySchedule(), givenAppeal)
        }

        assertEquals(expectedErrorCode, thrown.code)
    }

    @ParameterizedTest(name = "자기소개: {0}")
    @MethodSource("invalidAppeals")
    @DisplayName("자기소개 수정 시, 앞뒤 공백 제거 기준, 자기소개가 유효하지 않은 경우 예외 발생")
    fun shouldThrowRecruitmentAppealEmpty_whenUpdateWithInvalidAppeal(caseName: String, givenAppeal: String, expectedErrorCode: String) {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(
                createRecruitingStudySchedule(), "유효한 자기 소개")

            created.updateAppeal(givenAppeal)
        }

        assertEquals(expectedErrorCode, thrown.code)
    }

    // ----- 승인

    @Test
    @DisplayName("신청이 승인된 경우, 승인 일시가 null 이거나 신청 일시 이전이면 안 된다")
    fun shouldSetApprovedAtAfterRequestedAt_whenApproveRecruitment() {
        val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "예시 자기 소개")
        Thread.sleep(10)
        created.updateRecruitStatus(RecruitStatus.APPROVED)

        assertAll(
            { assertEquals(RecruitStatus.APPROVED, created.recruitStatus) },
            { assertNotNull(created.updatedAt) },
            { assertTrue { created.createdAt.isBefore(created.updatedAt) || created.createdAt.isEqual(created.updatedAt) } }
        )
    }

    @ParameterizedTest(name = "신청 상태: {0}")
    @MethodSource("recruitmentStatusesCannotBeApproved")
    @DisplayName("승인 불가한 신청 상태에서, 승인을 시도하면, 예외 발생 - BusinessException(RECRUITMENT_INVALID_STATUS_CHANGE)")
    fun shouldThrowRecruitmentInvalidStatusChange_whenApproveWithUnapprovableStatus(caseName: String, givenRecruitmentStatus: RecruitStatus, expectedErrorCode: String) {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(givenRecruitmentStatus)

            created.updateRecruitStatus(RecruitStatus.APPROVED)
        }

        assertEquals(expectedErrorCode, thrown.code)
    }
    
    // ----- 취소

    @ParameterizedTest(name = "신청 상태: {0}")
    @MethodSource("recruitmentStatusesCannotBeCancelled")
    @DisplayName("취소 불가한 신청 상태에서, 취소를 시도하면, 예외 발생 - BusinessException(RECRUITMENT_INVALID_STATUS_CHANGE)")
    fun shouldThrowRecruitmentInvalidStatusChange_whenCancelWithUncancellableStatus() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.CANCELLED)

            created.updateRecruitStatus(RecruitStatus.CANCELLED)
        }

        assertEquals(StudyDomainErrorCode.RECRUITMENT_INVALID_STATUS_CHANGE.code, thrown.code)
    }

    // ----- 반려

    @ParameterizedTest(name = "신청 상태: {0}")
    @MethodSource("recruitmentStatusesCannotBeRejected")
    @DisplayName("반려 불가한 신청 상태에서, 반려를 시도하면, 예외 발생 - BusinessException(RECRUITMENT_INVALID_STATUS_CHANGE)")
    fun shouldThrowRecruitmentInvalidStatusChange_whenRejectWithUnrejectableStatus() {
        val thrown = assertThrows<BusinessException> {
            val created = createStudyRecruitmentWithAppeal(createRecruitingStudySchedule(), "자기소개")
            created.updateRecruitStatus(RecruitStatus.CANCELLED)

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
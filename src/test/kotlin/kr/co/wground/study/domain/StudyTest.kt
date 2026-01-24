package kr.co.wground.study.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.constant.BudgetType
import kr.co.wground.study.domain.constant.Months
import kr.co.wground.study.domain.constant.StudyStatus
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

@DisplayName("스터디(Study) 테스트")
class StudyTest {

    @Nested
    @DisplayName("내부 요구조건 테스트")
    inner class StudyInternalConditionTest {

        // To Do: 특정 값을 변경하고 싶지 않은 경우, null 을 명시적으로 전달하는 방식이 괜찮을 지 모르겠음
        val NOT_GONNA_CHANGE = null;

        // ----- factories for test

        private fun createRecruitingStudySchedule(): StudySchedule {
            return StudySchedule(
                trackId = 3L,
                months = Months.FIRST,
                recruitStartDate = LocalDate.now().minusDays(4),
                recruitEndDate = LocalDate.now().plusDays(3),
                studyEndDate = LocalDate.now().plusDays(26)
            )
        }

        private fun createAlreadyStartedStudySchedule(): StudySchedule {
            return StudySchedule(
                trackId = 3L,
                months = Months.THIRD,
                recruitStartDate = LocalDate.now().minusDays(7),
                recruitEndDate = LocalDate.now().minusDays(3),
                studyEndDate = LocalDate.now().plusDays(21)
            )
        }

        private fun createStudyWithCapacity(schedule: StudySchedule, capacity: Int): Study {
            return Study(
                capacity = capacity,
                budget = BudgetType.BOOK,
                name = "스터디 제목",
                description = "스터디 소개글",
                leaderId = 1L,
                trackId = 3L,
                scheduleId = schedule.id,
                status = StudyStatus.PENDING
            )
        }

        private fun createStudyWithName(schedule: StudySchedule, name: String): Study {
            return Study(
                budget = BudgetType.BOOK,
                name = name,
                description = "스터디 소개글",
                leaderId = 1L,
                trackId = 3L,
                scheduleId = schedule.id,
                status = StudyStatus.PENDING
            )
        }

        private fun createStudyWithDescription(schedule: StudySchedule, description: String): Study {
            return Study(
                budget = BudgetType.BOOK,
                name = "유효한 제목",
                description = description,
                leaderId = 1L,
                trackId = 3L,
                scheduleId = schedule.id,
                status = StudyStatus.PENDING
            )
        }

        private fun createStudyWithExternalChatUrl(schedule: StudySchedule, externalChatUrl: String): Study {
            return Study(
                budget = BudgetType.BOOK,
                name = "유효한 제목",
                description = "유효한 소개글",
                leaderId = 1L,
                trackId = 3L,
                scheduleId = schedule.id,
                status = StudyStatus.PENDING,
                externalChatUrl = externalChatUrl
            )
        }

        private fun createStudyWithReferenceUrl(schedule: StudySchedule, referenceUrl: String): Study {
            return Study(
                budget = BudgetType.BOOK,
                name = "유효한 제목",
                description = "유효한 소개글",
                leaderId = 1L,
                trackId = 3L,
                scheduleId = schedule.id,
                status = StudyStatus.PENDING,
                referenceUrl = referenceUrl
            )
        }

        // ----- helpers

        private fun updateStudyCapacity(study: Study, capacity: Int, isRecruitmentClosed: Boolean) {
            return study.updateStudyInfo(
                newCapacity = capacity,
                newName = study.name,
                newDescription = study.description,
                newBudget = study.budget,
                newChatUrl = study.externalChatUrl,
                newRefUrl = study.referenceUrl,
                newTags = NOT_GONNA_CHANGE,
                newScheduleId = study.scheduleId,
                isRecruitmentClosed = isRecruitmentClosed
            )
        }

        private fun updateStudyName(study: Study, name: String, isRecruitmentClosed: Boolean) {
            return study.updateStudyInfo(
                newCapacity = study.capacity,
                newName = name,
                newDescription = study.description,
                newBudget = study.budget,
                newChatUrl = study.externalChatUrl,
                newRefUrl = study.referenceUrl,
                newTags = NOT_GONNA_CHANGE,
                newScheduleId = study.scheduleId,
                isRecruitmentClosed = isRecruitmentClosed
            )
        }

        private fun updateStudyDescription(study: Study, description: String, isRecruitmentClosed: Boolean) {
            return study.updateStudyInfo(
                newCapacity = study.capacity,
                newName = study.name,
                newDescription = description,
                newBudget = study.budget,
                newChatUrl = study.externalChatUrl,
                newRefUrl = study.referenceUrl,
                newTags = NOT_GONNA_CHANGE,
                newScheduleId = study.scheduleId,
                isRecruitmentClosed = isRecruitmentClosed
            )
        }

        private fun updateStudyExternalChatUrl(study: Study, externalChatUrl: String, isRecruitmentClosed: Boolean) {
            return study.updateStudyInfo(
                newCapacity = study.capacity,
                newName = study.name,
                newDescription = study.description,
                newBudget = study.budget,
                newChatUrl = externalChatUrl,
                newRefUrl = study.referenceUrl,
                newTags = NOT_GONNA_CHANGE,
                newScheduleId = study.scheduleId,
                isRecruitmentClosed = isRecruitmentClosed
            )
        }

        private fun updateStudyReferenceUrl(study: Study, referenceUrl: String, isRecruitmentClosed: Boolean) {
            return study.updateStudyInfo(
                newCapacity = study.capacity,
                newName = study.name,
                newDescription = study.description,
                newBudget = study.budget,
                newChatUrl = study.externalChatUrl,
                newRefUrl = referenceUrl,
                newTags = NOT_GONNA_CHANGE,
                newScheduleId = study.scheduleId,
                isRecruitmentClosed = isRecruitmentClosed
            )
        }
        
        // ----- 정원 수

        @Test
        fun `스터디 생성 시, 정원 수가 MIN_CAPACITY 미만이면, 예외 발생 - BusinessException(SD-0005)`() {

            // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(createRecruitingStudySchedule(), Study.MIN_CAPACITY - 1)
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_TOO_SMALL.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 정원 수가 MIN_CAPACITY 미만이면, 예외 발생 - BusinessException(SD-0005)`() {
            // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithCapacity(schedule, Study.MIN_CAPACITY)
                updateStudyCapacity(
                    created, Study.MIN_CAPACITY - 1, schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_TOO_SMALL.code, thrown.code)
        }

        @Test
        fun `스터디 생성 시, 정원 수가 ABSOLUTE_MAX_CAPACITY 초과이면, 예외 발생 - BusinessException(SD-0010)`() {
            
            // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(createRecruitingStudySchedule(), Study.ABSOLUTE_MAX_CAPACITY + 1)
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_TOO_BIG.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 정원 수가 ABSOLUTE_MAX_CAPACITY 초과이면, 예외 발생 - BusinessException(SD-0010)`() {

            // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithCapacity(schedule, Study.ABSOLUTE_MAX_CAPACITY)

                updateStudyCapacity(
                    created, Study.ABSOLUTE_MAX_CAPACITY + 1, schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_TOO_BIG.code, thrown.code)
        }

        @Test
        fun `모집 기간이 마감되지 않은, 참여 인원 수가 정원 수와 같고 상태가 CLOSED 인 스터디에서, 참여 인원 수가 감소한 경우, 해당 스터디의 상태는 PENDING 으로 변경된다`() {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithCapacity(schedule, 2)
            created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
            assertEquals(StudyStatus.CLOSED, created.status)

            created.decreaseMemberCount(schedule.isRecruitmentClosed())
            assertEquals(StudyStatus.PENDING, created.status)
        }

        @Test
        fun `모집 기간이 마감되지 않은, 참여 인원 수가 정원 수와 같고 상태가 CLOSED 인 스터디에서, 정원 수가 증가한 경우, 해당 스터디의 상태를 PENDING 으로 변경한다`() {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithCapacity(schedule, 2)
            created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
            assertEquals(StudyStatus.CLOSED, created.status)

            updateStudyCapacity(created, 3, schedule.isRecruitmentClosed())
            assertEquals(StudyStatus.PENDING, created.status)
        }

        // ----- 제목

        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 제목이 blank 인 경우, 예외 발생 - BusinessException(SD-0003)`() {

            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName(createRecruitingStudySchedule(), "  \t  \n  ")
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        // ❌
        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 제목의 길이가 2자 미만이면, 예외 발생 - BusinessException(SD-0003)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName(createRecruitingStudySchedule(),"  \t 1 \n  ")
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 제목의 길이가 MAX_NAME_LENGTH 자를 초과하면, 예외 발생 - BusinessException(SD-0003)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName(
                    createRecruitingStudySchedule(),
                    "  \t " + "*".repeat(Study.MAX_NAME_LENGTH + 1) + " \n  "
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 제목이 blank 인 경우, 예외 발생 - BusinessException(SD-0003)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithName(schedule, "  \t 유효한 제목 \n  ")

                updateStudyName(created, " \t \n ", schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        // ❌
        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 제목의 길이가 2자 미만이면 예외 발생 - BusinessException(SD-0003)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithName(schedule, "  \t 유효한 제목 \n  ")

                updateStudyName(created, "1", schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 제목의 길이가 MAX_NAME_LENGTH 자를 초과하면, 예외 발생 - BusinessException(SD-0003)`() {
            val schedule = createRecruitingStudySchedule()
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName(schedule, "  \t 유효한 제목 \n  ")

                updateStudyName(
                    created,
                    "*".repeat(Study.MAX_NAME_LENGTH + 1),
                    schedule.isRecruitmentClosed()
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        // ----- 소개 글

        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 소개글이 blank 인 경우, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithDescription(createRecruitingStudySchedule(), " \t \n ")
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        // ❌
        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준 소개글의 길이자 2자 미만이면, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithDescription(createRecruitingStudySchedule(), "A")
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 소개글의 길이가 MAX_DESCRIPTION_LENGTH 를 초과하면, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithDescription(
                    createRecruitingStudySchedule(),
                    "*".repeat(Study.MAX_DESCRIPTION_LENGTH + 1))
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }
        
        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 소개글이 blank 인 경우, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithDescription(schedule, "유효한 소개글")

                updateStudyDescription(
                    created, " \t \n ", schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        // ❌
        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 소개글의 길이가 2자 미만이면, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithDescription(schedule, "유효한 소개글")

                updateStudyDescription(
                    created, "A", schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 소개글의 길이가 MAX_DESCRIPTION_LENGTH 자를 초과하면, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithDescription(
                    schedule, " \t \n " + "*".repeat(Study.MAX_DESCRIPTION_LENGTH))

                updateStudyDescription(
                    created,
                    "*".repeat(Study.MAX_DESCRIPTION_LENGTH + 1),
                    schedule.isRecruitmentClosed()
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        // ----- 채팅 방 링크

        @Test
        fun `스터디 생성 시, 채팅 방 링크가 null 이 아닐 때 해당 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(SD-0006)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithExternalChatUrl(
                    createRecruitingStudySchedule(), "유효하지 않은 형식의 링크")
            }

            assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 채팅 방 링크가 null 이 아닐 때 해당 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(SD-0006)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithExternalChatUrl(
                    schedule, "https://www.kakaocorp.com/page/service/service/openchat")

                updateStudyExternalChatUrl(
                    created,
                    "유효하지 않은 형식의 링크",
                    schedule.isRecruitmentClosed()
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
        }

        // ----- 참고 자료 링크

        @Test
        fun `스터디 생성 시, 참고 자료 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(SD-0006)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithReferenceUrl(
                    createRecruitingStudySchedule(), "유효하지 않은 형식의 링크")
            }

            assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 참고 자료 링크가 null 이 아닐 때 해당 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(SD-0006)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithReferenceUrl(
                    schedule, "https://www.kakaocorp.com/page/service/service/openchat")

                updateStudyReferenceUrl(
                    created,
                    "유효하지 않은 형식의 링크",
                    schedule.isRecruitmentClosed()
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
        }

        // ----- 거부 테스트

        // ❌ 예외가 발생하지 않음
        // To Do: 에러 코드 필요함 - STUDY_CANT_BE_REJECTED_IN_APPROVED_STATUS
        @Test
        fun `대상 스터디가 APPROVED 상태일 때, 스터디 거부 시, 예외 발생 - BusinessException()`() {

            val thrown = assertThrows<BusinessException> {

                // given
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithCapacity(schedule, 2)
                created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
                created.approve()

                // when
                // To Do: approve 상태에서 reject 시 예외 처리 누락
                created.reject()
            }

            // To Do: STUDY_CANT_BE_REJECTED_IN_APPROVED_STATUS 에러 코드 일치 여부 확인
        }
        
        // ----- 결재 테스트

        @Test
        fun `대상 스터디가 PENDING 상태일 때, 스터디 결재 시, 예외 발생 - BusinessException()`() {

            val thrown = assertThrows<BusinessException> {
                val pending = createStudyWithCapacity(createRecruitingStudySchedule(),2)
                pending.approve()
            }

            assertEquals(StudyDomainErrorCode.STUDY_MUST_BE_CLOSED_TO_APPROVE.code, thrown.code)
        }

        @Test
        fun `대상 스터디가 REJECTED 상태일 때, 스터디 결재 시, 예외 발생 - BusinessException()`() {

            val thrown = assertThrows<BusinessException> {
                val rejected = createStudyWithCapacity(createRecruitingStudySchedule(), 2)
                rejected.reject()
                rejected.approve()
            }

            assertEquals(StudyDomainErrorCode.STUDY_MUST_BE_CLOSED_TO_APPROVE.code, thrown.code)
        }

        // ----- 참여 인원 수 테스트 (스터디 신청과의 정합성 고려 x)

        @Test
        fun `참여 인원 수가 (정원 - 1)인 경우, 참여 인원 수 1 증가 시, CLOSED 상태로 성공적으로 변경된다`() {
            val schedule = createRecruitingStudySchedule()
            val created = createStudyWithCapacity(schedule, 2)
            created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
            assertEquals(StudyStatus.CLOSED, created.status)
        }

        @Test
        fun `REJECTED 상태에서, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(SD-0001)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithCapacity(schedule, 2)
                created.reject()
                created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_NOT_RECRUITING.code, thrown.code)
        }

        @Test
        fun `APPROVED 상태에서, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(SD-0001)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithCapacity(schedule, 2)
                created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
                created.approve()

                created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_NOT_RECRUITING.code, thrown.code)
        }

        // ❌ SD-0001 이 발생하고 있음
        @Test
        fun `이미 정원이 가득 찬 경우, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(SD-0002)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithCapacity(schedule,2)
                created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
                created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_FULL.code, thrown.code)
        }

        @Test
        fun `이미 모집 기간이 마감된 경우, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(SD-0014)`() {
            val thrown = assertThrows<BusinessException> {
                val alreadyStartedSchedule = createAlreadyStartedStudySchedule()
                val created = createStudyWithName(alreadyStartedSchedule, "유효한 이름")

                created.increaseMemberCount(
                    alreadyStartedSchedule.recruitEndDate,
                    alreadyStartedSchedule.isRecruitmentClosed()
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_ALREADY_FINISH_TO_RECRUIT.code, thrown.code)
        }
        
        // ----- 수정 테스트

        @Test
        fun `REJECTED 상태인 경우, 스터디를 수정 시, 예외 발생 - BusinessException(SD-0009)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithCapacity(schedule,2)
                created.reject()
                updateStudyName(created, "제목제목", schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_AFTER_DETERMINED.code, thrown.code)
        }

        @Test
        fun `APPROVED 상태인 경우, 스터디를 수정 시, 예외 발생 - BusinessException(SD-0009)`() {
            val thrown = assertThrows<BusinessException> {
                val schedule = createRecruitingStudySchedule()
                val created = createStudyWithCapacity(schedule, 2)
                created.increaseMemberCount(schedule.recruitEndDate, schedule.isRecruitmentClosed())
                created.approve()

                updateStudyName(created, "제목제목", schedule.isRecruitmentClosed())
            }

            assertEquals(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_AFTER_DETERMINED.code, thrown.code)
        }
    }
}
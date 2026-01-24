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
import java.lang.reflect.Modifier
import java.time.LocalDate

@DisplayName("스터디(Study) 테스트")
class StudyTest {

    @Nested
    @DisplayName("내부 요구조건 테스트")
    inner class StudyInternalConditionTest {

        // To Do: 특정 값을 변경하고 싶지 않은 경우, null 을 명시적으로 전달하는 방식이 괜찮을 지 모르겠음
        val NOT_GONNA_CHANGE = null;

        // ----- 정원 수

        @Test
        fun `스터디 생성 시, 정원 수가 MIN_CAPACITY 미만이면, 예외 발생 - BusinessException(SD-0005)`() {

            // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(extractMinCapacityFromStudyClass() - 1)
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_TOO_SMALL.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 정원 수가 MIN_CAPACITY 미만이면, 예외 발생 - BusinessException(SD-0005)`() {
            // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(extractMinCapacityFromStudyClass())
                updateStudyCapacity(created, extractMinCapacityFromStudyClass() - 1)
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_TOO_SMALL.code, thrown.code)
        }

        @Test
        fun `스터디 생성 시, 정원 수가 ABSOLUTE_MAX_CAPACITY 초과이면, 예외 발생 - BusinessException(SD-0010)`() {
            
            // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(extractAbsoluteMaxCapacityFromStudyClass() + 1)
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_TOO_BIG.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 정원 수가 ABSOLUTE_MAX_CAPACITY 초과이면, 예외 발생 - BusinessException(SD-0010)`() {

            // 주어진 사용자가 해당 트랙에 참가 중이라고 가정
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(extractAbsoluteMaxCapacityFromStudyClass())

                updateStudyCapacity(created, extractAbsoluteMaxCapacityFromStudyClass() + 1)
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_TOO_BIG.code, thrown.code)
        }

        @Test
        fun `모집 기간이 마감되지 않은, 참여 인원 수가 정원 수와 같고 상태가 CLOSED 인 스터디에서, 참여 인원 수가 감소한 경우, 해당 스터디의 상태는 PENDING 으로 변경된다`() {
            val created = createStudyWithCapacity(2)
            created.increaseMemberCount()
            assertEquals(StudyStatus.CLOSED, created.status)

            created.decreaseMemberCount()
            assertEquals(StudyStatus.PENDING, created.status)
        }

        @Test
        fun `모집 기간이 마감되지 않은, 참여 인원 수가 정원 수와 같고 상태가 CLOSED 인 스터디에서, 정원 수가 증가한 경우, 해당 스터디의 상태를 PENDING 으로 변경한다`() {
            val created = createStudyWithCapacity(2)
            created.increaseMemberCount()
            assertEquals(StudyStatus.CLOSED, created.status)

            updateStudyCapacity(created, 3)
            assertEquals(StudyStatus.PENDING, created.status)
        }

        private fun createStudyWithCapacity(capacity: Int): Study {
            return Study(
                capacity = capacity,
                budget = BudgetType.BOOK,
                name = "스터디 제목",
                description = "스터디 소개글",
                leaderId = 1L,
                trackId = 3L,
                schedule = StudySchedule(
                    trackId = 3L,
                    months = Months.THIRD,
                    recruitStartDate = LocalDate.now().minusDays(4),
                    recruitEndDate = LocalDate.now().plusDays(3),
                    studyEndDate = LocalDate.now().plusDays(26)
                ),
                status = StudyStatus.PENDING
            )
        }

        private fun updateStudyCapacity(study: Study, capacity: Int) {
            return study.updateStudyInfo(
                newCapacity = capacity,
                newName = NOT_GONNA_CHANGE,
                newDescription = NOT_GONNA_CHANGE,
                newBudget = NOT_GONNA_CHANGE,
                newChatUrl = NOT_GONNA_CHANGE,
                newRefUrl = NOT_GONNA_CHANGE,
                newTags = NOT_GONNA_CHANGE
            )
        }

        private fun extractMinCapacityFromStudyClass(): Int {

            runCatching {
                val f = Study::class.java.getDeclaredField("MIN_CAPACITY").apply { isAccessible = true }
                return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(null) // 거의 static
            }

            val companionInstance = Tag::class.java.getDeclaredField("Companion")
                .apply { isAccessible = true }
                .get(null) // static 필드라 null

            val f = companionInstance.javaClass.getDeclaredField("MIN_CAPACITY").apply { isAccessible = true }
            return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(companionInstance)
        }

        private fun extractAbsoluteMaxCapacityFromStudyClass(): Int {

            runCatching {
                val f = Study::class.java.getDeclaredField("ABSOLUTE_MAX_CAPACITY").apply { isAccessible = true }
                return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(null) // 거의 static
            }

            val companionInstance = Tag::class.java.getDeclaredField("Companion")
                .apply { isAccessible = true }
                .get(null) // static 필드라 null

            val f = companionInstance.javaClass.getDeclaredField("ABSOLUTE_MAX_CAPACITY").apply { isAccessible = true }
            return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(companionInstance)
        }

        // ----- 제목

        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 제목이 blank 인 경우, 예외 발생 - BusinessException(SD-0003)`() {

            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName("  \t  \n  ")
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        // ❌
        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 제목의 길이가 2자 미만이면, 예외 발생 - BusinessException(SD-0003)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName("  \t 1 \n  ")
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 제목의 길이가 MAX_NAME_LENGTH 자를 초과하면, 예외 발생 - BusinessException(SD-0003)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName(
                    "  \t " + "*".repeat(extractMaxNameLengthFromStudyClass() + 1) + " \n  "
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 제목이 blank 인 경우, 예외 발생 - BusinessException(SD-0003)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName("  \t 유효한 제목 \n  ")

                updateStudyName(created, "   \t   \n  ")
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        // ❌
        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 제목의 길이가 2자 미만이면 예외 발생 - BusinessException(SD-0003)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName("  \t 유효한 제목 \n  ")

                updateStudyName(created, "1")
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 제목의 길이가 MAX_NAME_LENGTH 자를 초과하면, 예외 발생 - BusinessException(SD-0003)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithName("  \t 유효한 제목 \n  ")

                updateStudyName(created, "*".repeat(extractMaxNameLengthFromStudyClass() + 1))
            }

            assertEquals(StudyDomainErrorCode.STUDY_NAME_INVALID.code, thrown.code)
        }

        private fun createStudyWithName(name: String): Study {
            return Study(
                budget = BudgetType.BOOK,
                name = name,
                description = "스터디 소개글",
                leaderId = 1L,
                trackId = 3L,
                schedule = StudySchedule(
                    trackId = 3L,
                    months = Months.THIRD,
                    recruitStartDate = LocalDate.now().minusDays(4),
                    recruitEndDate = LocalDate.now().plusDays(3),
                    studyEndDate = LocalDate.now().plusDays(26)
                ),
                status = StudyStatus.PENDING
            )
        }

        private fun updateStudyName(study: Study, name: String) {
            return study.updateStudyInfo(
                newCapacity = NOT_GONNA_CHANGE,
                newName = name,
                newDescription = NOT_GONNA_CHANGE,
                newBudget = NOT_GONNA_CHANGE,
                newChatUrl = NOT_GONNA_CHANGE,
                newRefUrl = NOT_GONNA_CHANGE,
                newTags = NOT_GONNA_CHANGE
            )
        }

        private fun extractMaxNameLengthFromStudyClass(): Int {

            runCatching {
                val f = Study::class.java.getDeclaredField("MAX_NAME_LENGTH").apply { isAccessible = true }
                return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(null) // 거의 static
            }

            val companionInstance = Tag::class.java.getDeclaredField("Companion")
                .apply { isAccessible = true }
                .get(null) // static 필드라 null

            val f = companionInstance.javaClass.getDeclaredField("MAX_NAME_LENGTH").apply { isAccessible = true }
            return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(companionInstance)
        }

        // ----- 소개 글

        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 소개글이 blank 인 경우, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithDescription("   \t  \n ")
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        // ❌
        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준 소개글의 길이자 2자 미만이면, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithDescription("1")
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 생성 시, 앞뒤 공백 제거 기준, 소개글의 길이가 MAX_DESCRIPTION_LENGTH 를 초과하면, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithDescription(
                    "*".repeat(extractMaxDescriptionLengthFromStudyClass() + 1))
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }
        
        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 소개글이 blank 인 경우, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithDescription(
                    "유효한 소개글")

                updateStudyDescription(created, "    \t    \n  ")
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        // ❌
        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 소개글의 길이가 2자 미만이면, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithDescription(
                    "유효한 소개글")

                updateStudyDescription(created, "1")
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 앞뒤 공백 제거 기준, 소개글의 길이가 MAX_DESCRIPTION_LENGTH 자를 초과하면, 예외 발생 - BusinessException(SD-0004)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithDescription(
                    " \t \n " + "*".repeat(extractMaxDescriptionLengthFromStudyClass()))

                updateStudyDescription(
                    created,
                    "*".repeat(extractMaxDescriptionLengthFromStudyClass() + 1)
                )
            }

            assertEquals(StudyDomainErrorCode.STUDY_DESCRIPTION_INVALID.code, thrown.code)
        }

        private fun createStudyWithDescription(description: String): Study {
            return Study(
                budget = BudgetType.BOOK,
                name = "유효한 제목",
                description = description,
                leaderId = 1L,
                trackId = 3L,
                schedule = StudySchedule(
                    trackId = 3L,
                    months = Months.THIRD,
                    recruitStartDate = LocalDate.now().minusDays(4),
                    recruitEndDate = LocalDate.now().plusDays(3),
                    studyEndDate = LocalDate.now().plusDays(26)
                ),
                status = StudyStatus.PENDING
            )
        }

        private fun updateStudyDescription(study: Study, description: String) {
            return study.updateStudyInfo(
                newCapacity = NOT_GONNA_CHANGE,
                newName = NOT_GONNA_CHANGE,
                newDescription = description,
                newBudget = NOT_GONNA_CHANGE,
                newChatUrl = NOT_GONNA_CHANGE,
                newRefUrl = NOT_GONNA_CHANGE,
                newTags = NOT_GONNA_CHANGE
            )
        }

        private fun extractMaxDescriptionLengthFromStudyClass(): Int {

            runCatching {
                val f = Study::class.java.getDeclaredField("MAX_DESCRIPTION_LENGTH").apply { isAccessible = true }
                return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(null) // 거의 static
            }

            val companionInstance = Tag::class.java.getDeclaredField("Companion")
                .apply { isAccessible = true }
                .get(null) // static 필드라 null

            val f = companionInstance.javaClass.getDeclaredField("MAX_DESCRIPTION_LENGTH").apply { isAccessible = true }
            return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(companionInstance)
        }

        // ----- 채팅 방 링크

        @Test
        fun `스터디 생성 시, 채팅 방 링크가 null 이 아닐 때 해당 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(SD-0006)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithExternalChatUrl("유효하지 않은 형식의 링크")
            }

            assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 채팅 방 링크가 null 이 아닐 때 해당 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(SD-0006)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithExternalChatUrl("https://www.kakaocorp.com/page/service/service/openchat")

                updateStudyExternalChatUrl(created, "유효하지 않은 형식의 링크")
            }

            assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
        }

        private fun createStudyWithExternalChatUrl(externalChatUrl: String): Study {
            return Study(
                budget = BudgetType.BOOK,
                name = "유효한 제목",
                description = "유효한 소개글",
                leaderId = 1L,
                trackId = 3L,
                schedule = StudySchedule(
                    trackId = 3L,
                    months = Months.THIRD,
                    recruitStartDate = LocalDate.now().minusDays(4),
                    recruitEndDate = LocalDate.now().plusDays(3),
                    studyEndDate = LocalDate.now().plusDays(26)
                ),
                status = StudyStatus.PENDING,
                externalChatUrl = externalChatUrl
            )
        }

        private fun updateStudyExternalChatUrl(study: Study, externalChatUrl: String) {
            return study.updateStudyInfo(
                newCapacity = NOT_GONNA_CHANGE,
                newName = NOT_GONNA_CHANGE,
                newDescription = NOT_GONNA_CHANGE,
                newBudget = NOT_GONNA_CHANGE,
                newChatUrl = externalChatUrl,
                newRefUrl = NOT_GONNA_CHANGE,
                newTags = NOT_GONNA_CHANGE
            )
        }

        // ----- 참고 자료 링크

        @Test
        fun `스터디 생성 시, 참고 자료 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(SD-0006)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithReferenceUrl("유효하지 않은 형식의 링크")
            }

            assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
        }

        @Test
        fun `스터디 수정 시, 참고 자료 링크가 null 이 아닐 때 해당 링크의 형식이 유효한 url 형식이 아닌 경우, 예외 발생 - BusinessException(SD-0006)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithReferenceUrl("https://www.kakaocorp.com/page/service/service/openchat")

                updateStudyReferenceUrl(created, "유효하지 않은 형식의 링크")
            }

            assertEquals(StudyDomainErrorCode.STUDY_URL_INVALID.code, thrown.code)
        }

        private fun createStudyWithReferenceUrl(referenceUrl: String): Study {
            return Study(
                budget = BudgetType.BOOK,
                name = "유효한 제목",
                description = "유효한 소개글",
                leaderId = 1L,
                trackId = 3L,
                schedule = StudySchedule(
                    trackId = 3L,
                    months = Months.THIRD,
                    recruitStartDate = LocalDate.now().minusDays(4),
                    recruitEndDate = LocalDate.now().plusDays(3),
                    studyEndDate = LocalDate.now().plusDays(26)
                ),
                status = StudyStatus.PENDING,
                referenceUrl = referenceUrl
            )
        }

        private fun updateStudyReferenceUrl(study: Study, referenceUrl: String) {
            return study.updateStudyInfo(
                newCapacity = NOT_GONNA_CHANGE,
                newName = NOT_GONNA_CHANGE,
                newDescription = NOT_GONNA_CHANGE,
                newBudget = NOT_GONNA_CHANGE,
                newChatUrl = NOT_GONNA_CHANGE,
                newRefUrl = referenceUrl,
                newTags = NOT_GONNA_CHANGE
            )
        }

        // ----- 거부 테스트

        // ❌ 예외가 발생하지 않음
        // To Do: 에러 코드 필요함 - STUDY_CANT_BE_REJECTED_IN_APPROVED_STATUS
        @Test
        fun `대상 스터디가 APPROVED 상태일 때, 스터디 거부 시, 예외 발생 - BusinessException()`() {

            val thrown = assertThrows<BusinessException> {

                // given
                val created = createStudyWithCapacity(2)
                created.increaseMemberCount()
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
                val pending = createStudyWithCapacity(2)
                pending.approve()
            }

            assertEquals(StudyDomainErrorCode.STUDY_MUST_BE_CLOSED_TO_APPROVE.code, thrown.code)
        }

        @Test
        fun `대상 스터디가 REJECTED 상태일 때, 스터디 결재 시, 예외 발생 - BusinessException()`() {

            val thrown = assertThrows<BusinessException> {
                val rejected = createStudyWithCapacity(2)
                rejected.reject()
                rejected.approve()
            }

            assertEquals(StudyDomainErrorCode.STUDY_MUST_BE_CLOSED_TO_APPROVE.code, thrown.code)
        }

        // ----- 참여 인원 수 테스트 (스터디 신청과의 정합성 고려 x)

        @Test
        fun `참여 인원 수가 (정원 - 1)인 경우, 참여 인원 수 1 증가 시, CLOSED 상태로 성공적으로 변경된다`() {
            val created = createStudyWithCapacity(2)
            created.increaseMemberCount()
            assertEquals(StudyStatus.CLOSED, created.status)
        }

        @Test
        fun `REJECTED 상태에서, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(SD-0001)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(2)
                created.reject()
                created.increaseMemberCount()
            }

            assertEquals(StudyDomainErrorCode.STUDY_NOT_RECRUITING.code, thrown.code)
        }

        @Test
        fun `APPROVED 상태에서, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(SD-0001)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(2)
                created.increaseMemberCount()
                created.approve()

                created.increaseMemberCount()
            }

            assertEquals(StudyDomainErrorCode.STUDY_NOT_RECRUITING.code, thrown.code)
        }

        // ❌ SD-0001 이 발생하고 있음
        @Test
        fun `이미 정원이 가득 찬 경우, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(SD-0002)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(2)
                created.increaseMemberCount()
                created.increaseMemberCount()
            }

            assertEquals(StudyDomainErrorCode.STUDY_CAPACITY_FULL.code, thrown.code)
        }

        @Test
        fun `이미 모집 기간이 마감된 경우, 참여 인원 수 1 증가 시, 예외 발생 - BusinessException(SD-0014)`() {
            val thrown = assertThrows<BusinessException> {
                val created = Study(
                    budget = BudgetType.BOOK,
                    name = "유효한 제목",
                    description = "유효한 소개글",
                    leaderId = 1L,
                    trackId = 3L,
                    schedule = StudySchedule(
                        trackId = 3L,
                        months = Months.THIRD,
                        recruitStartDate = LocalDate.now().minusDays(7),
                        recruitEndDate = LocalDate.now().minusDays(3),
                        studyEndDate = LocalDate.now().plusDays(21)
                    ),
                    status = StudyStatus.PENDING
                )

                created.increaseMemberCount()
            }

            assertEquals(StudyDomainErrorCode.STUDY_ALREADY_FINISH_TO_RECRUIT.code, thrown.code)
        }
        
        // ----- 수정 테스트

        @Test
        fun `REJECTED 상태인 경우, 스터디를 수정 시, 예외 발생 - BusinessException(SD-0009)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(2)
                created.reject()
                updateStudyName(created, "제목제목")
            }

            assertEquals(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_AFTER_DETERMINED.code, thrown.code)
        }

        @Test
        fun `APPROVED 상태인 경우, 스터디를 수정 시, 예외 발생 - BusinessException(SD-0009)`() {
            val thrown = assertThrows<BusinessException> {
                val created = createStudyWithCapacity(2)
                created.increaseMemberCount()
                created.approve()
                updateStudyName(created, "제목제목")
            }

            assertEquals(StudyDomainErrorCode.STUDY_CANNOT_MODIFY_AFTER_DETERMINED.code, thrown.code)
        }
    }
}
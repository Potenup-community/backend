package kr.co.wground.study.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.reflect.Modifier
import java.util.stream.Stream
import kotlin.test.assertEquals

@DisplayName("태그(Tag) 테스트")
class StudyTagTest {

    companion object {
        @JvmStatic
        fun invalidTagNames(): Stream<Arguments> = Stream.of(
            Arguments.of("empty", "", StudyDomainErrorCode.TAG_FORMAT_INVALID.code),
            Arguments.of("blank(space)", " ", StudyDomainErrorCode.TAG_FORMAT_INVALID.code),
            Arguments.of("blank(tab)", "\t", StudyDomainErrorCode.TAG_FORMAT_INVALID.code),
            Arguments.of("blank(newline)", "\n", StudyDomainErrorCode.TAG_FORMAT_INVALID.code),
            Arguments.of("blank(mixed)", " \t \n ", StudyDomainErrorCode.TAG_FORMAT_INVALID.code),
            Arguments.of("blank(not allowed characters)", "!@$%^&*()\n\t ", StudyDomainErrorCode.TAG_FORMAT_INVALID.code),
            Arguments.of("too short(${minTagNameLength()}자 미만)", "A".repeat(minTagNameLength() - 1), StudyDomainErrorCode.TAG_LENGTH_INVALID_RANGE.code),
            Arguments.of("too short(${minTagNameLength()}자 미만; trimmed)", " A ", StudyDomainErrorCode.TAG_LENGTH_INVALID_RANGE.code),
            Arguments.of("too short(${minTagNameLength()}자 미만; mixed)", " \t!@$%^&*() A \n ", StudyDomainErrorCode.TAG_LENGTH_INVALID_RANGE.code),
            Arguments.of("too long(${maxTagNameLength()}자 초과)", " \t" + "A".repeat(maxTagNameLength() + 1) + "\n ", StudyDomainErrorCode.TAG_LENGTH_INVALID_RANGE.code)
        )

        private fun minTagNameLength(): Int {

            runCatching {
                val f = Tag::class.java.getDeclaredField("MIN_LENGTH").apply { isAccessible = true }
                return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(null) // 거의 static
            }

            val companionInstance = Tag::class.java.getDeclaredField("Companion")
                .apply { isAccessible = true }
                .get(null) // static 필드라 null

            val f = companionInstance.javaClass.getDeclaredField("MIN_LENGTH").apply { isAccessible = true }
            return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(companionInstance)
        }

        private fun maxTagNameLength(): Int {

            runCatching {
                val f = Tag::class.java.getDeclaredField("MAX_LENGTH").apply { isAccessible = true }
                return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(null) // 거의 static
            }

            val companionInstance = Tag::class.java.getDeclaredField("Companion")
                .apply { isAccessible = true }
                .get(null) // static 필드라 null

            val f = companionInstance.javaClass.getDeclaredField("MAX_LENGTH").apply { isAccessible = true }
            return if (Modifier.isStatic(f.modifiers)) f.getInt(null) else f.getInt(companionInstance)
        }
    }

    @Test
    @DisplayName("태그 생성 시, 공백 문자와 허용되지 않은 특수 문자가 제외되고, 대문자가 소문자로 변환된다")
    fun shouldSanitizeAndLowercase_whenCreateTag() {
        val created = Tag.create(rawName = "  \t  +#._-*****ABC******가나다 \n   ")

        assertEquals("+#._-abc가나다", created.name)
    }

    @ParameterizedTest(name = "태그 이름: {0}")
    @MethodSource("invalidTagNames")
    @DisplayName("태그 생성 시, 태그 이름이 유효하지 않은 경우, 예외 발생")
    fun shouldThrowTagFormatInvalid_whenCreateTagWithInvalidTagName(caseName: String, givenTagName: String, expectedErrorCode: String) {
        val thrown = assertThrows<BusinessException> {
            Tag.create(rawName = givenTagName)
        }

        assertEquals(expectedErrorCode, thrown.code)
    }
}
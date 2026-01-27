package kr.co.wground.study.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.exception.StudyDomainErrorCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.reflect.Modifier
import kotlin.test.assertEquals

@DisplayName("태그(Tag) 테스트")
class StudyTagTest {

    @Test
    fun `태그 생성 시, 공백 문자와 허용되지 않은 특수 문자가 제외되고, 대문자가 소문자로 변환된다`() {
        val created = Tag.create(rawName = "  \t  +#._-*****ABC******가나다 \n   ")

        assertEquals("+#._-abc가나다", created.name)
    }

    @Test
    fun `태그 생성 시, 태그 이름이 공백 문자를 제외하고 비어있는 경우, 예외 발생 - BusinessException(TG-0001)`() {
        val thrown = assertThrows<BusinessException> {
            Tag.create(rawName = "\n  \t")
        }

        assertEquals(StudyDomainErrorCode.TAG_FORMAT_INVALID.code, thrown.code)
    }

    @Test
    fun `태그 생성 시, 공백 문자를 제외한 태그 이름의 길이가 MIN_LENGTH 보다 작은 경우, 예외 발생 - BusinessException(TG-0002)`() {

        val MIN_LENGTH = extractMinLengthFromTagClass()

        val thrown = assertThrows<BusinessException> {
            Tag.create(rawName = "   " + "a".repeat(MIN_LENGTH - 1) + "\t\n")
        }

        assertEquals(StudyDomainErrorCode.TAG_LENGTH_INVALID_RANGE.code, thrown.code)
    }

    @Test
    fun `태그 생성 시, 공백 문자를 제외한 태그 이름의 길이가 MAX_LENGTH 보다 큰 경우, 예외 발생 - BusinessException(TG-0002)`() {

        val MAX_LENGTH = extractMaxLengthFromTagClass()

        val thrown = assertThrows<BusinessException> {
            Tag.create(rawName = "   " + "a".repeat(MAX_LENGTH + 1) + "\t\n")
        }

        assertEquals(StudyDomainErrorCode.TAG_LENGTH_INVALID_RANGE.code, thrown.code)
    }

    // ----- helpers

    private fun extractMinLengthFromTagClass(): Int {

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

    private fun extractMaxLengthFromTagClass(): Int {

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
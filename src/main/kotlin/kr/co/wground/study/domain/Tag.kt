package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.exception.BusinessException
import kr.co.wground.study.domain.exception.StudyDomainErrorCode

@Entity
class Tag protected constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @Column(unique = true, nullable = false)
    val name: String
) {
    companion object {
        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 15
        private val SPECIAL_CHAR_LIST = listOf('+', '#', '.', '_', '-')
        private val VALID_PATTERN = Regex("^[가-힣a-z0-9+#._-]+$")

        fun create(rawName: String): Tag {
            val normalizedName = normalize(rawName)
            validateTag(normalizedName)
            return Tag(name = normalizedName)
        }

        fun normalize(input: String): String {
            return input.trim()
                .lowercase()
                .replace(" ", "")
                .filter {
                    it.isDigit() ||
                            it.isLowerCase() ||
                            it in '가'..'힣' ||
                            it in SPECIAL_CHAR_LIST
                }
        }

        private fun validateTag(name: String) {
            if (name.isBlank()) {
                throw BusinessException(StudyDomainErrorCode.TAG_FORMAT_INVALID)
            }
            println("******* ${name}")
            println("******* ${name.length}")
            if (name.length !in MIN_LENGTH..MAX_LENGTH) {
                throw BusinessException(StudyDomainErrorCode.TAG_LENGTH_INVALID_RANGE)
            }
            if (!VALID_PATTERN.matches(name)) {
                throw BusinessException(StudyDomainErrorCode.TAG_FORMAT_INVALID)
            }
        }
    }
}
package kr.co.wground.post.domain.enums

enum class Topic(
    val description: String
) {
    NOTICE(description = "공지사항"),
    KNOWLEDGE(description = "지식줍줍"),
    EMPLOYMENT_TIP(description = "취업팁"),
}

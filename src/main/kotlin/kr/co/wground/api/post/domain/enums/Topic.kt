package kr.co.wground.api.post.domain.enums

enum class Topic(
    val description: String
) {
    CONFERENCE(description = "컨퍼런스"),
    KNOWLEDGE(description = "지식줍줍"),
    EMPLOYMENT_TIP(description = "취업팁"),
}
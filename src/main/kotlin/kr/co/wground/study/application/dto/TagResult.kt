package kr.co.wground.study.application.dto

import kr.co.wground.study.domain.Tag

data class TagResult(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(tag: Tag): TagResult {
            return TagResult(
                id = tag.id,
                name = tag.name
            )
        }
    }
}

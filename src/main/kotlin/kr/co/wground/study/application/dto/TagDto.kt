package kr.co.wground.study.application.dto

import kr.co.wground.study.domain.Tag

data class TagDto(
    val id: Long,
    val name: String
) {
    companion object {
        fun from(tag: Tag): TagDto {
            return TagDto(
                id = tag.id,
                name = tag.name
            )
        }
    }
}

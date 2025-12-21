package kr.co.wground.post.infra

import kr.co.wground.post.domain.Post
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.SliceImpl

interface CustomPostRepository {
    fun findAllByPageable(pageable: Pageable): SliceImpl<Post>
}

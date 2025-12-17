package kr.co.wground.comment.infra

import kr.co.wground.comment.domain.vo.CommentCount
import kr.co.wground.global.common.PostId

interface CommentRepositoryCustom {
    fun countByPostIds(postIds: List<PostId>): List<CommentCount>
}

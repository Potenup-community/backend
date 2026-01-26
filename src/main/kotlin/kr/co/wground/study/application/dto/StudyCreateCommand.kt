package kr.co.wground.study.application.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.constant.BudgetType

data class StudyCreateCommand(
    val userId: UserId,
    val name: String,
    val description: String,
    val capacity: Int,
    val budget: BudgetType,
    val chatUrl: String,
    val refUrl: String?,
    val tags: List<String> = emptyList()
)
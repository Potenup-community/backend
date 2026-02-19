package kr.co.wground.study.application.dto

import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.enums.BudgetType

data class StudyCreateCommand(
    val userId: UserId,
    val name: String,
    val description: String,
    val capacity: Int,
    val budget: BudgetType,
    val budgetExplain: String,
    val chatUrl: String,
    val refUrl: String?,
    val week1Plan: String,
    val week2Plan: String,
    val week3Plan: String,
    val week4Plan: String,
    val tags: List<String> = emptyList()
)

package kr.co.wground.study.application.dto

import kr.co.wground.global.common.UserId

data class StudyReportUpsertCommand(
    val studyId: Long,
    val userId: UserId,
    val week1Activity: String,
    val week2Activity: String,
    val week3Activity: String,
    val week4Activity: String,
    val retrospectiveGood: String,
    val retrospectiveImprove: String,
    val retrospectiveNextAction: String,
)

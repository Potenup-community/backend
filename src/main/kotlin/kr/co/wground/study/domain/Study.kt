package kr.co.wground.study.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import kr.co.wground.global.common.UserId
import kr.co.wground.study.domain.constant.StudyStatus
import java.time.LocalDateTime

@Entity
class Study (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val leaderId : UserId,
    @Column(columnDefinition = "TEXT", nullable = false, length = 300)
    val description: String,
    val status: StudyStatus,
    val hiringAmount: Integer,
    val externalChatUrl: String,
    val referenceUrl: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
){
}
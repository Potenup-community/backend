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
class Study(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    name: String,
    val leaderId: UserId,
    description: String,
    status: StudyStatus,
    hiringAmount: Integer,
    externalChatUrl: String,
    referenceUrl: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    var name: String = name
        protected set

    @Column(columnDefinition = "TEXT", nullable = false, length = 300)
    var description: String = description
        protected set

    var status: StudyStatus = status
        protected set

    var hiringAmount: Integer = hiringAmount
        protected set

    var externalChatUrl: String = externalChatUrl
        protected set

    var referenceUrl: String = referenceUrl
        protected set

    var updatedAt: LocalDateTime = updatedAt
        protected set
}
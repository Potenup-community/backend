package kr.co.wground.study.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class StudySchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0


}
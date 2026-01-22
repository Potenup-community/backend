package kr.co.wground.study.infra

import kr.co.wground.study.domain.StudySchedule

interface CustomScheduleRepository {
    fun findAll(): List<StudySchedule>
    fun getById(id: Long): StudySchedule?

}
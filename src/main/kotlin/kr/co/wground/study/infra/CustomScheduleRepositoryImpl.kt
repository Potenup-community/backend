package kr.co.wground.study.infra

import kr.co.wground.study.domain.QStudySchedule.studySchedule
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.study.domain.StudySchedule
import org.springframework.stereotype.Repository

@Repository
class CustomScheduleRepositoryImpl(
    private val queryFactory: JPAQueryFactory
): CustomScheduleRepository{
    override fun findAll(): List<StudySchedule>{
        val result = queryFactory.select().from(studySchedule)
        return TODO()
    }

    override fun getById(id: Long): StudySchedule? {
        TODO("Not yet implemented")
    }
}
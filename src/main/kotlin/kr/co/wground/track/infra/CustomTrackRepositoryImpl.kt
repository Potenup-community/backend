package kr.co.wground.track.infra

import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQueryFactory
import kr.co.wground.track.domain.QTrack.track
import kr.co.wground.track.domain.Track
import org.springframework.stereotype.Repository

@Repository
class CustomTrackRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomTrackRepository {
    override fun findAllTracks(): List<Track> {
        return queryFactory
            .selectFrom(track)
            .orderBy(
                CaseBuilder()
                    .`when`(track.trackId.eq(1L)).then(0)
                    .otherwise(1)
                    .asc(),
                track.endDate.desc()
            )
            .fetch()
    }
}
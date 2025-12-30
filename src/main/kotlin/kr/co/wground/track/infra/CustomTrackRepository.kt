package kr.co.wground.track.infra

import kr.co.wground.track.domain.Track

interface CustomTrackRepository {
    fun findAllTracks(): List<Track>
}
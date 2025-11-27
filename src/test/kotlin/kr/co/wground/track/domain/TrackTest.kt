package kr.co.wground.track.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.domain.exception.TrackDomainErrorCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

class TrackTest {

    @Test
    @DisplayName("트랙 일정 수정 시 상태가 현재 날짜를 기준으로 갱신된다")
    fun updateTrack_shouldRefreshStatusWithProvidedNow() {
        // given
        val track = Track(
            trackName = "Backend",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 6, 30),
        )

        // when
        track.updateTrack(
            trackName = "Backend Advanced",
            startDate = LocalDate.of(2024, 3, 1),
            endDate = LocalDate.of(2024, 9, 30),
            now = LocalDate.of(2024, 10, 1),
        )

        // then
        assertEquals("Backend Advanced", track.trackName)
        assertEquals(LocalDate.of(2024, 3, 1), track.startDate)
        assertEquals(LocalDate.of(2024, 9, 30), track.endDate)
        assertEquals(TrackStatus.GRADUATED, track.trackStatus)
    }

    @Test
    @DisplayName("트랙 이름이 공백이면 BusinessException이 발생한다")
    fun updateTrack_shouldThrowWhenNameIsBlank() {
        // given
        val track = Track(
            trackName = "Backend",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 6, 30),
        )

        // when
        val exception = assertThrows(BusinessException::class.java) {
            track.updateTrack(
                trackName = " ",
                startDate = null,
                endDate = null,
            )
        }

        // then
        assertEquals(TrackDomainErrorCode.TRACK_NAME_IS_BLANK.message, exception.message)
    }

    @Test
    @DisplayName("트랙의 시작일이 종료일보다 늦으면 BusinessException이 발생한다")
    fun updateTrack_shouldThrowWhenDateRangeInvalid() {
        // given
        val track = Track(
            trackName = "Backend",
            startDate = LocalDate.of(2024, 1, 1),
            endDate = LocalDate.of(2024, 6, 30),
        )

        // when
        val exception = assertThrows(BusinessException::class.java) {
            track.updateTrack(
                trackName = null,
                startDate = LocalDate.of(2024, 7, 1),
                endDate = LocalDate.of(2024, 6, 30),
            )
        }

        // then
        assertEquals(TrackDomainErrorCode.INVALID_DATE_RANGE.message, exception.message)
    }
}

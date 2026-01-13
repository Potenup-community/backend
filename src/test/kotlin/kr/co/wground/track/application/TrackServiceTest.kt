package kr.co.wground.track.application

import kr.co.wground.exception.BusinessException
import kr.co.wground.track.application.dto.CreateTrackDto
import kr.co.wground.track.application.dto.UpdateTrackDto
import kr.co.wground.track.application.exception.TrackServiceErrorCode
import kr.co.wground.track.domain.Track
import kr.co.wground.track.infra.TrackRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDate

class TrackServiceTest {

    private val trackRepository = mock(TrackRepository::class.java)
    private val eventPublisher = mock(ApplicationEventPublisher::class.java)
    private lateinit var trackService: TrackServiceImpl

    @BeforeEach
    fun setUp() {
        trackService = TrackServiceImpl(trackRepository, eventPublisher)
    }

    @Test
    @DisplayName("트랙 생성 성공")
    fun createTrack_Success() {
        // given
        val createDto = CreateTrackDto(
            trackName = "BE 1기",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(6)
        )
        val savedTrack = Track(
            trackName = createDto.trackName,
            startDate = createDto.startDate,
            endDate = createDto.endDate
        )

        `when`(trackRepository.save(any(Track::class.java))).thenReturn(savedTrack)
        `when`(trackRepository.findAllTracks()).thenReturn(listOf(savedTrack))

        // when
        val result = trackService.createTrack(createDto)

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0].trackName).isEqualTo(createDto.trackName)
        verify(eventPublisher).publishEvent(any())
    }

    @Test
    @DisplayName("트랙 수정 성공")
    fun updateTrack_Success() {
        // given
        val trackId = 1L
        val updateDto = UpdateTrackDto(
            trackId = trackId,
            trackName = "BE 1기 수정",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(7)
        )
        val track = Track(
            trackName = "BE 1기",
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(6)
        )

        `when`(trackRepository.findByIdOrNull(trackId)).thenReturn(track)

        // when
        trackService.updateTrack(updateDto)

        // then
        assertThat(track.trackName).isEqualTo(updateDto.trackName)
        verify(eventPublisher).publishEvent(any())
    }

    @Test
    @DisplayName("트랙 수정 실패 - 트랙 없음")
    fun updateTrack_NotFound() {
        // given
        val trackId = 1L
        val updateDto = UpdateTrackDto(
            trackId = trackId,
            trackName = "BE 1기 수정"
        )

        `when`(trackRepository.findByIdOrNull(trackId)).thenReturn(null)

        // when & then
        assertThatThrownBy { trackService.updateTrack(updateDto) }
            .isInstanceOf(BusinessException::class.java)
            .extracting("errorCode")
            .isEqualTo(TrackServiceErrorCode.TRACK_NOT_FOUND)
    }
}

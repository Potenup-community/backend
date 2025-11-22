package kr.co.wground.like.application

import kr.co.wground.like.application.dto.LikeCreateDto
import kr.co.wground.like.infra.LikeJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@ActiveProfiles("test")
@SpringBootTest
class LikeServiceConcurrencyTest {

    @Autowired
    private lateinit var likeService: LikeService

    @Autowired
    private lateinit var likeRepository: LikeJpaRepository

    @AfterEach
    fun tearDown() {
        likeRepository.deleteAll()
    }

    @Test
    fun `동시에 100개의 좋아요 요청이 와도 하나의 좋아요만 남고 정상적으로 처리된다`() {
        // given
        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32)
        val doneLatch = CountDownLatch(threadCount)

        val dto = LikeCreateDto(
            userId = 1L,
            postId = 1L
        )

        // when
        (1..threadCount).forEach { i ->
            executorService.submit {
                try {
                    likeService.likePost(dto)
                } finally {
                    doneLatch.countDown()
                }
            }
        }

        val completed = doneLatch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()

        // then
        assertThat(completed).isTrue()

        val likeCount = likeRepository.count()
        assertThat(likeCount).isEqualTo(1)
    }
}

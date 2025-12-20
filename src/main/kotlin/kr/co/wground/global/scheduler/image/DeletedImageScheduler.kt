package kr.co.wground.global.scheduler.image

import jakarta.transaction.Transactional
import kr.co.wground.image.application.ImageStorageService
import kr.co.wground.image.infra.ImageRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Component
class DeletedImageScheduler(
    private val imageFileRepository: ImageRepository,
    private val imageStorageService: ImageStorageService
) {
    @Scheduled(fixedDelay = 60_0000)
    @Transactional
    fun deleteExpiredTempImages() {
        val cutoff = LocalDateTime.now().minus(24, ChronoUnit.HOURS)
        val targets = imageFileRepository.findTempOlderThan(cutoff)

        targets.forEach { img ->
            imageStorageService.deleteByRelativePath(img.relativePath)
        }

        imageFileRepository.deleteAll(targets)
    }
}

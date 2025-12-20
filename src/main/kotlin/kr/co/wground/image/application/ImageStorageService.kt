package kr.co.wground.image.application

import kr.co.wground.common.SyncDraftImagesToPostEvent
import kr.co.wground.global.common.OwnerId
import kr.co.wground.image.application.dto.LocalStoredDto
import kr.co.wground.image.application.dto.UploadImageDto
import kr.co.wground.image.domain.ImageFile
import kr.co.wground.image.infra.ImageRepository
import kr.co.wground.image.policy.UploadPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID.randomUUID

@Service
@Transactional
class ImageStorageService(
    private val props: UploadPolicy,
    private val imageRepository: ImageRepository
) {
    companion object{
        private val MD_IMAGE_URL_REGEX = Regex("""!\[[^\]]*]\(([^)]+)\)""")
    }

    fun saveTemp(dto: UploadImageDto): LocalStoredDto {
        val id = randomUUID().toString()
        val ext = extFromMime(dto.imageFile.contentType) ?: "bin"

        val relativePath = relativePath(dto.ownerId, id, ext)
        val targetPath = Path.of(props.localDir, relativePath)

        Files.createDirectories(targetPath.parent)

        dto.imageFile.inputStream.use { input ->
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        val url = uploadedUrl(relativePath)

        val imageFile = ImageFile.create(dto.ownerId, dto.draftId, relativePath)
        imageRepository.save(imageFile)

        return LocalStoredDto(id = id, relativePath = relativePath, url = url)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    fun syncDraftImageFiles(dto: SyncDraftImagesToPostEvent) {
        val urls = extractUploadUrls(dto.markdown)
        val usedRelativePaths = urls.map { toRelativePath(it) }.toSet()

        val currentlyLinked = imageRepository.findAllByDraftIdAndOwnerId(dto.draftId, dto.ownerId)

        //TODO(Repository에 in 쿼리로 마크하는 부분으로 수정 예정 -> 이유는 현재 모든 엔티티를 들고와서 어플리케이션에서 작업중 하지만 그것보다 그냥 바로 in query를 하는것이 더 나을 수 있음
        currentlyLinked.filter { it.relativePath in usedRelativePaths }
            .forEach {
                it.markUsed()
                it.fillPostId(dto.postId)
            }

        currentlyLinked.filter { it.relativePath !in usedRelativePaths }
            .forEach {it.markOrphan()}
    }

    private fun extractUploadUrls(markdown: String): Set<String> {
        return MD_IMAGE_URL_REGEX.findAll(markdown)
            .map { it.groupValues[1].trim() }
            .filter { it.startsWith(props.publicBasePath) }
            .toSet()
    }

    private fun toRelativePath(url: String): String {
        return url.removePrefix(props.publicBasePath).trimStart('/') // tmp/1/uuid.jpg
    }

    private fun uploadedUrl(relativePath: String): String = "${props.publicBasePath.trimEnd('/')}/$relativePath"
    private fun relativePath(ownerId: OwnerId, id: String, ext: String) ="tmp/$ownerId/$id.$ext"

    private fun extFromMime(mime: String?): String? = when (mime?.lowercase()) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/gif" -> "gif"
        "image/webp" -> "webp"
        else -> null
    }
}


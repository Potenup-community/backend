package kr.co.wground.image.application

import kr.co.wground.common.event.SyncDraftImagesToPostEvent
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.OwnerId
import kr.co.wground.image.application.dto.LocalStoredDto
import kr.co.wground.image.application.dto.UploadImageDto
import kr.co.wground.image.domain.ImageFile
import kr.co.wground.image.exception.DeleteImageErrorCode
import kr.co.wground.image.infra.ImageRepository
import kr.co.wground.image.infra.dto.MarkOrphanByDraftDto
import kr.co.wground.image.infra.dto.MarkUsedByDraftDto
import kr.co.wground.image.policy.UploadPolicy
import kr.co.wground.image.validator.ImageUploadValidator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.UUID.randomUUID

@Service
@Transactional
class ImageStorageService(
    private val props: UploadPolicy,
    private val imageRepository: ImageRepository,
    private val validator: ImageUploadValidator,
) {
    companion object {
        private val MD_IMAGE_URL_REGEX = Regex("""!\[[^\]]*]\(([^)]+)\)""")
        private val log = LoggerFactory.getLogger(ImageStorageService::class.java)
    }

    fun saveTemp(dto: UploadImageDto): LocalStoredDto {
        validator.validate(dto.imageFile)

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

        return LocalStoredDto(imageId = id, relativePath = relativePath, url = url)
    }

    @Transactional(propagation = Propagation.MANDATORY)
    fun saveProjectThumbnail(ownerId: OwnerId, imageFile: MultipartFile): String {
        validator.validate(imageFile)

        val id = randomUUID().toString()
        val ext = extFromMime(imageFile.contentType) ?: "bin"
        val relativePath = "projects/$ownerId/$id.$ext"
        val targetPath = Path.of(props.localDir, relativePath)

        Files.createDirectories(targetPath.parent)
        imageFile.inputStream.use { input ->
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
        }

        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCompletion(status: Int) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    runCatching { Files.deleteIfExists(targetPath) }
                        .onFailure { log.error("롤백 후 썸네일 삭제 실패: $targetPath", it) }
                }
            }
        })

        return relativePath
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    fun syncDraftImageFiles(dto: SyncDraftImagesToPostEvent) {
        val urls = extractUploadUrls(dto.markdown)
        val usedRelativePaths = urls.map { toRelativePath(it) }.toSet()

        MarkUsedByDraftDto(
            ownerId = dto.ownerId,
            draftId = dto.draftId,
            postId = dto.postId,
            paths = usedRelativePaths,
        ).also {
            imageRepository.markUsedAndFillPostIdByDraft(it)
        }

        MarkOrphanByDraftDto(
            postId = dto.postId,
            ownerId = dto.ownerId,
            draftId = dto.draftId,
            paths = usedRelativePaths,
        ).also {
            imageRepository.markOrphanByDraftNotInPaths(it)
        }
    }

    fun deleteByRelativePath(relativePath: String) {
        val baseDir = Path.of(props.localDir)
            .toAbsolutePath()
            .normalize()

        val targetPath = baseDir
            .resolve(relativePath)
            .normalize()

        validateRelativePath(targetPath, baseDir)
        validateDirectory(targetPath)

        try {
            Files.deleteIfExists(targetPath)
        } catch (e: Exception) {
            log.error("Failed to delete file: $targetPath, reason=${e.message}")
        }
    }

    private fun validateDirectory(targetPath: Path) {
        if (Files.exists(targetPath) && Files.isDirectory(targetPath)) {
            throw BusinessException(DeleteImageErrorCode.REFUSE_TO_DELETE_DIRECTORY)
        }
    }

    private fun validateRelativePath(targetPath: Path, baseDir: Path) {
        if (!targetPath.startsWith(baseDir)) {
            throw BusinessException(DeleteImageErrorCode.INVALID_RELATIVE_PATH)
        }
    }


    private fun extractUploadUrls(markdown: String): Set<String> {
        return MD_IMAGE_URL_REGEX.findAll(markdown)
            .map { it.groupValues[1].trim() }
            .filter {
                it.startsWith(props.publicBasePath) || it.startsWith(fullPublicPrefix())
            }
            .toSet()
    }

    private fun toRelativePath(url: String): String {
        val full = fullPublicPrefix()
        val removed = when {
            url.startsWith(full) -> url.removePrefix(full)
            url.startsWith(props.publicBasePath) -> url.removePrefix(props.publicBasePath)
            else -> url
        }
        return removed.trimStart('/')
    }

    private fun uploadedUrl(relativePath: String): String {
        val base = props.baseUrl.trimEnd('/')
        val path = props.publicBasePath.trimEnd('/')
        val rel = relativePath.trimStart('/')
        return if (base.isBlank()) "$path/$rel" else "$base$path/$rel"
    }

    private fun relativePath(ownerId: OwnerId, id: String, ext: String) = "tmp/$ownerId/$id.$ext"

    private fun extFromMime(mime: String?): String? = when (mime?.lowercase()) {
        "image/jpeg" -> "jpg"
        "image/png" -> "png"
        "image/gif" -> "gif"
        "image/webp" -> "webp"
        else -> null
    }

    private fun fullPublicPrefix(): String =
        "${props.baseUrl.trimEnd('/')}${props.publicBasePath}"
}


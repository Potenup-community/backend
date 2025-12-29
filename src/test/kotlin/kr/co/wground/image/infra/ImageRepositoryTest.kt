package kr.co.wground.image.infra

import jakarta.persistence.EntityManager
import kr.co.wground.global.common.OwnerId
import kr.co.wground.global.config.QueryDslConfig
import kr.co.wground.image.domain.ImageFile
import kr.co.wground.image.domain.enums.ImageStatus
import kr.co.wground.image.infra.dto.MarkOrphanByDraftDto
import kr.co.wground.image.infra.dto.MarkUsedByDraftDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig::class)
class ImageRepositoryTest(
) {
    @Autowired
    lateinit var repo: ImageRepository
    @Autowired
    lateinit var em: EntityManager

    @AfterEach
    fun cleanUp() {
        repo.deleteAllInBatch()
    }

    @DisplayName("마크된 경로들이 포함된 이미지들은 USED 상태로 변경된다.")
    @Test
    fun shouldMarkedUsedWhenRelativePathIsIn(){
        //given
        val ownerId = 1L
        val draftId = UUID.randomUUID()
        val postId = 1L
        val image1 = persist(ownerId, draftId, "path1")
        val image2 = persist(ownerId, draftId, "path2")
        val image3 = persist(ownerId, draftId, "path3")

        repo.saveAll(listOf(image1, image2, image3))
        em.flush()
        em.clear()

        val dto = MarkUsedByDraftDto(
            ownerId = ownerId,
            draftId = draftId,
            postId = postId,
            paths = setOf("path1", "path3"),
        )

        //when
        repo.markUsedAndFillPostIdByDraft(dto)
        val foundEntities = repo.findAll()

        //then
        assertEquals(2, foundEntities.filter { it.status == ImageStatus.USED }.size)
    }

    @DisplayName("마크된 경로들이 포함되지 이미지들은 ORPHAN 상태로 변경된다.")
    @Test
    fun shouldMarkedOrphanWhenRelativePathIsNotIn(){
        //given
        val ownerId = 1L
        val draftId = UUID.randomUUID()
        val postId = 1L
        val image1 = persist(ownerId, draftId, "path1")
        val image2 = persist(ownerId, draftId, "path2")
        val image3 = persist(ownerId, draftId, "path3")

        repo.saveAll(listOf(image1, image2, image3))
        em.flush()
        em.clear()

        val dto = MarkOrphanByDraftDto(
            ownerId = ownerId,
            draftId = draftId,
            postId = postId,
            paths = setOf("path1"),
        )

        //when
        repo.markOrphanByDraftNotInPaths(dto)

        val foundEntities = repo.findAll()

        //then
        assertEquals(2, foundEntities.filter { it.status == ImageStatus.ORPHAN }.size)
    }

    @DisplayName("이전에 사용되었던 이미지 경로라도, 마크된 경로들에 포함되지 않으면 ORPHAN 상태로 변경된다.")
    @Test
    fun shouldMarkedOrphanWhenRelativePathIsNotInEvenIfUsedBefore(){
        //given
        val ownerId = 1L
        val postId = 1L
        val draftId1 = UUID.randomUUID()
        val imageBeforeUpdate1 = persist(ownerId, draftId1, "path1")
        val imageBeforeUpdate2 = persist(ownerId, draftId1, "path2")
        val imageBeforeUpdate3 = persist(ownerId, draftId1, "path3")

        val draftId2 = UUID.randomUUID()
        val imageAfterUpdate1 = persist(ownerId, draftId2, "path4")
        val imageAfterUpdate2 = persist(ownerId, draftId2, "path5")
        val imageAfterUpdate3 = persist(ownerId, draftId2, "path6")

        repo.saveAll(listOf(
            imageBeforeUpdate1,
            imageBeforeUpdate2,
            imageBeforeUpdate3,
            imageAfterUpdate1,
            imageAfterUpdate2,
            imageAfterUpdate3,
        ))
        em.flush()
        em.clear()

        val dto1 = MarkUsedByDraftDto(
            ownerId = ownerId,
            draftId = draftId1,
            postId = postId,
            paths = setOf("path1", "path2", "path3"),
        )

        val dto2 = MarkOrphanByDraftDto(
            ownerId = ownerId,
            draftId = draftId2,
            postId = postId,
            paths = setOf("path1", "path2"),
        )

        //when
        repo.markUsedAndFillPostIdByDraft(dto1)
        repo.markOrphanByDraftNotInPaths(dto2)

        val foundEntities = repo.findAll()
        //then
        assertEquals(4, foundEntities.filter { it.status == ImageStatus.ORPHAN }.size)
    }

    private fun persist(
        ownerId: OwnerId,
        draftId: UUID,
        relativePath: String,
    ): ImageFile {
        return ImageFile.create(ownerId, draftId, relativePath)
    }
}

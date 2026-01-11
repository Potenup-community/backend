package kr.co.wground.post.infra

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import kr.co.wground.config.QuerydslTestConfig
import kr.co.wground.post.domain.Post
import kr.co.wground.post.domain.PostStatus
import kr.co.wground.post.domain.QPost.post
import kr.co.wground.post.domain.enums.HighlightType
import kr.co.wground.post.domain.enums.Topic
import kr.co.wground.post.infra.predicate.GetPostSummaryPredicate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import kotlin.math.pow

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(QuerydslTestConfig::class)
class PostRepositoryTest(
    @Autowired private val em: EntityManager,
    @Autowired private val jpaQueryFactory: JPAQueryFactory,
    @Autowired private val postRepository: PostRepository
) {
    companion object {
        @Container
        @JvmStatic
        val mysql = MySQLContainer("mysql:8.0").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
        }

        @JvmStatic
        @DynamicPropertySource
        fun mysqlProps(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }

            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("spring.jpa.properties.hibernate.dialect") {
                "org.hibernate.dialect.MySQLDialect"
            }
        }
    }

    @Test
    @DisplayName("popularity desc 정렬이 기대대로 동작한다")
    fun shouldSortByPopularityDescWhenPopularityIsRequested() {
        // given
        val dbNowTs = em.createNativeQuery("select now()").singleResult as java.sql.Timestamp
        val now = dbNowTs.toLocalDateTime()

        val p1 = newPost(
            title = "p1",
            createdAt = now.minusHours(1),
            reactionCount = 3,
            recentViewCount = 10
        )
        val p2 = newPost(
            title = "p2",
            createdAt = now.minusHours(5),
            reactionCount = 20,
            recentViewCount = 1
        )
        val p3 = newPost(
            title = "p3",
            createdAt = now.minusHours(2),
            reactionCount = 1,
            recentViewCount = 200
        )

        postRepository.saveAll(listOf(p1, p2, p3))
        em.flush()
        em.clear()

        val pageable = PageRequest.of(
            0,
            10,
            Sort.by(Sort.Order.desc("popularity"))
        )

        val predicate = GetPostSummaryPredicate(
            pageable = pageable,
            topic = null
        )

        // when
        val slice = postRepository.findAllByPredicate(predicate)
        val orderedIds = slice.content.map { it.id }

        // then
        val scoreExpr = popularityExprForTest()

        val scoreMap = jpaQueryFactory
            .select(post.id, scoreExpr)
            .from(post)
            .where(post.id.`in`(orderedIds))
            .fetch()
            .associate { row -> row.get(post.id)!! to row.get(scoreExpr)!! }

        val scoresInSliceOrder = orderedIds.map { scoreMap[it]!! }

        assertThat(scoresInSliceOrder)
            .isSortedAccordingTo(compareByDescending<Double> { it })
    }

    @Test
    @DisplayName("createdAt desc 정렬 + tie-breaker id desc가 적용된다")
    fun shouldApplyIdDescAsTieBreakerWhenCreatedAtIsSame() {
        // given
        val t = LocalDateTime.now().minusHours(2)

        val p1 = newPost(title = "a", createdAt = t, reactionCount = 0, recentViewCount = 0)
        val p2 = newPost(title = "b", createdAt = t, reactionCount = 0, recentViewCount = 0)

        em.persist(p1)
        em.persist(p2)
        em.flush()
        em.clear()

        val pageable = PageRequest.of(
            0,
            10,
            Sort.by(Sort.Order.desc("createdAt"))
        )
        val predicate = GetPostSummaryPredicate(pageable = pageable, topic = null)

        // when
        val slice = postRepository.findAllByPredicate(predicate)

        // then
        assertThat(slice.content).hasSize(2)

        val ids = slice.content.map { it.id }
        assertThat(ids).isSortedAccordingTo(compareByDescending { it })
    }

    @Test
    @DisplayName("Slice nextPage가 정확히 판단된다")
    fun shouldReturnHasNextTrueWhenMoreThanPageSizeIsFetched() {
        // given
        repeat(6) { idx ->
            em.persist(
                newPost(
                    title = "p$idx",
                    createdAt = LocalDateTime.now().minusHours(idx.toLong()),
                    reactionCount = idx,
                    recentViewCount = idx * 10
                )
            )
        }
        em.flush()
        em.clear()

        val pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.desc("createdAt")))
        val predicate = GetPostSummaryPredicate(pageable = pageable, topic = null)

        // when
        val slice = postRepository.findAllByPredicate(predicate)

        // then
        assertThat(slice.content).hasSize(5)
        assertThat(slice.hasNext()).isTrue
    }

    private fun newPost(
        title: String,
        createdAt: LocalDateTime,
        reactionCount: Int,
        recentViewCount: Int
    ): Post {
        val post = Post(
            writerId = 1L,
            title = title,
            content = "content",
            topic = Topic.EMPLOYMENT_TIP,
            createdAt = createdAt,
            deletedAt = null,
            postStatus = PostStatus(highlightType = HighlightType.NONE),
        )

        post.recentViewCount = recentViewCount

        setReactionCountByReflection(post, reactionCount)

        return post
    }

    private fun setReactionCountByReflection(post: Post, value: Int) {
        val f = Post::class.java.getDeclaredField("reactionCount")
        f.isAccessible = true
        f.setInt(post, value)
    }

    private fun popularityExprForTest(): NumberExpression<Double> {
        val a = 3.0
        val c = 2.0
        val alpha = 1.5

        val reaction = post.reactionCount.doubleValue()
        val viewCount = post.recentViewCount.doubleValue()

        val tHours = Expressions.numberTemplate(
            Double::class.java,
            "CAST(GREATEST(0, 1e0 * TIMESTAMPDIFF(HOUR, {0}, NOW())) AS DOUBLE)",
            post.createdAt
        )

        val ln1pViewCount = Expressions.numberTemplate(
            Double::class.java,
            "CAST(LN(1 + {0}) AS DOUBLE)",
            viewCount
        )

        val numerator = reaction.add(ln1pViewCount.multiply(a))

        val denominator = Expressions.numberTemplate(
            Double::class.java,
            "CAST(POW(({0} + {1}), {2}) AS DOUBLE)",
            tHours,
            c,
            alpha
        )

        return numerator.divide(denominator)
    }
}

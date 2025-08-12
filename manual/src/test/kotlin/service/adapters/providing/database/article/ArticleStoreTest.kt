package service.adapters.providing.database.article

import java.util.UUID.randomUUID
import org.approvej.ApprovalBuilder.approve
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait.forListeningPort
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import service.DataSourceProxyTestConfig
import service.adapters.providing.database.article.SqlStringsPrettyPrinter.Companion.sqlStringPrettyPrinter
import service.application.article.ArticleBuilder.Companion.anArticle
import service.application.article.ToStoreArticles

@Testcontainers
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  classes = [DataSourceProxyTestConfig::class],
)
class ArticleStoreTest(
  @param:Autowired private val articleStore: ToStoreArticles,
  @param:Autowired private val articleRepository: ArticleRepository,
  @param:Autowired private val queryCollector: DataSourceProxyTestConfig.QueryCollector,
) {

  @BeforeEach
  fun resetQueryCollector() {
    queryCollector.reset()
  }

  @Test
  fun storeArticle() {
    val article = anArticle().build()

    val storedArticle = articleStore.storeArticle(article)

    assertThat(storedArticle).isEqualTo(article)
    assertThat(articleRepository.findById(article.id)).isPresent()
    approve(queryCollector.queries()).printWith(sqlStringPrettyPrinter()).byFile()
  }

  @Test
  fun getArticleById() {
    val storedArticleEntity = articleRepository.save(ArticleEntity(anArticle().build()))

    val gottenArticle = articleStore.getArticleById(storedArticleEntity.id)

    assertThat(gottenArticle).isNotNull()
    approve(queryCollector.queries()).printWith(sqlStringPrettyPrinter()).byFile()
  }

  @Test
  fun `getArticleById unknown`() {
    assertThat(articleStore.getArticleById(randomUUID())).isNull()
    approve(queryCollector.queries()).printWith(sqlStringPrettyPrinter()).byFile()
  }

  @Test
  fun queryArticle() {
    val commonTitle = "common title"
    val storedArticlesWithCommonTitle =
      listOf(
        articleStore.storeArticle(anArticle().title("Super $commonTitle").build()),
        articleStore.storeArticle(anArticle().title("Mega $commonTitle").build()),
      )
    val otherStoredArticle = articleStore.storeArticle(anArticle().title("Other title").build())

    val foundArticle = articleStore.findArticles(commonTitle)

    assertThat(foundArticle)
      .containsExactlyInAnyOrderElementsOf(storedArticlesWithCommonTitle)
      .doesNotContain(otherStoredArticle)
    approve(queryCollector.queries()).printWith(sqlStringPrettyPrinter()).byFile()
  }

  companion object {
    @Container
    @JvmStatic
    val postgresql: PostgreSQLContainer<Nothing> =
      PostgreSQLContainer<Nothing>("postgres:latest").waitingFor(forListeningPort())
  }
}

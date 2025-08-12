package service.adapters.providing.database.article

import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait.forListeningPort
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import service.application.article.ArticleBuilder.Companion.anArticle
import service.application.article.ToStoreArticles

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArticleStoreTest(
  @param:Autowired private val articleRepository: ArticleRepository,
  @param:Autowired private val articleStore: ToStoreArticles,
) {

  @Test
  fun storeArticle() {
    val article = anArticle().build()

    val storedArticle = articleStore.storeArticle(article)

    assertThat(storedArticle).isEqualTo(article)
    assertThat(articleRepository.findById(article.id)).isPresent()
  }

  @Test
  fun getArticleById() {
    val storedArticleEntity = articleRepository.save(ArticleEntity(anArticle().build()))

    val gottenArticle = articleStore.getArticleById(storedArticleEntity.id)

    assertThat(gottenArticle).isNotNull()
  }

  @Test
  fun `getArticleById unknown`() {
    assertThat(articleStore.getArticleById(randomUUID())).isNull()
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
  }

  companion object {
    @Container
    @ServiceConnection
    @JvmStatic
    val postgresql: PostgreSQLContainer<Nothing> =
      PostgreSQLContainer<Nothing>("postgres:latest").waitingFor(forListeningPort())
  }
}

package service.application.article

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import service.application.article.ArticleBuilder.Companion.anArticle

class ArticleImporterTest {

  private val articleStore = ArticleStoreStub()
  private val articleImporter = ArticleImporter(articleStore)

  @Test
  fun importArticle() {
    val article = anArticle().build()

    articleImporter.importArticle(article)

    assertThat(articleStore.getArticleById(article.id)).isEqualTo(article)
  }
}

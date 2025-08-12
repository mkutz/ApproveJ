package service.application.article

import org.approvej.ApprovalBuilder.approve
import org.approvej.print.ObjectPrinter.objectPrinter
import org.approvej.scrub.Scrubbers.uuids
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import service.application.article.ArticleBuilder.Companion.anArticle

class ArticlesQueryTest {

  private val articleStoreStub = ArticleStoreStub()

  private val articleQuery = ArticlesQuery(articleStoreStub)

  @Test
  fun queryArticles() {
    val article = articleStoreStub.storeArticle(anArticle().build())

    val result = articleQuery.queryArticles(article.title)

    assertThat(result).containsExactly(article)

    approve(result).printWith(objectPrinter()).scrubbedOf(uuids()).byFile()
  }

  @Test
  fun `queryArticles multiple`() {
    val query = "Match"
    val articles =
      listOf(
          anArticle(),
          anArticle(),
          anArticle(),
          anArticle(),
          anArticle(),
          anArticle(),
          anArticle(),
        )
        .map { articleStoreStub.storeArticle(it.title("$query ${it.title}").build()) }

    val result = articleQuery.queryArticles(query)

    assertThat(result).containsAll(articles)

    approve(result).printWith(objectPrinter()).scrubbedOf(uuids()).byFile()
  }
}

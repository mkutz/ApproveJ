package service.application.article

import java.util.*

class ArticleStoreStub(private val data: MutableMap<UUID, Article> = mutableMapOf()) :
  ToStoreArticles {

  override fun storeArticle(article: Article): Article {
    data[article.id] = article
    return article
  }

  override fun getArticleById(articleId: UUID) = data[articleId]

  override fun findArticles(query: String) =
    data.values.filter { it.title.contains(query, ignoreCase = true) }
}

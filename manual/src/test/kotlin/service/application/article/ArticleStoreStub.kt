package service.application.article

import java.util.*

class ArticleStoreStub(private val data: MutableMap<UUID, Article> = mutableMapOf()) :
  ToStoreArticles {

  override fun getArticleById(articleId: UUID) = data[articleId]

  override fun queryArticle(query: String) =
    data.values.filter { it.title.contains(query, ignoreCase = true) }

  override fun storeArticle(article: Article): Article {
    data[article.id] = article
    return article
  }
}

package service.application.article

import java.util.*

interface ToStoreArticles {

  fun getArticleById(articleId: UUID): Article?

  fun queryArticle(query: String): List<Article>

  fun storeArticle(article: Article): Article
}

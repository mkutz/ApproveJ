package service.application.article

import org.springframework.stereotype.Component

@Component
class ArticlesQuery(private val articleStore: ToStoreArticles) : ToQueryArticles {

  override fun queryArticles(query: String) = articleStore.queryArticle(query)
}

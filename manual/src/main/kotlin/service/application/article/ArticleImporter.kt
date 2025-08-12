package service.application.article

import org.springframework.stereotype.Component

@Component
class ArticleImporter(private val articleStore: ToStoreArticles) : ToImportArticles {

  override fun importArticle(article: Article) {
    articleStore.storeArticle(article)
  }
}

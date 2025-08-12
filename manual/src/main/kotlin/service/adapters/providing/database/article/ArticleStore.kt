package service.adapters.providing.database.article

import java.util.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import service.application.article.Article
import service.application.article.ToStoreArticles

@Component
class ArticleStore(private val articleRepository: ArticleRepository) : ToStoreArticles {

  override fun getArticleById(articleId: UUID) =
    articleRepository.findByIdOrNull(articleId)?.toArticle()

  override fun findArticles(query: String) =
    articleRepository.findAllByTitleContainsIgnoreCase(query).map { it.toArticle() }

  override fun storeArticle(article: Article): Article =
    articleRepository.save(ArticleEntity(article)).toArticle()
}

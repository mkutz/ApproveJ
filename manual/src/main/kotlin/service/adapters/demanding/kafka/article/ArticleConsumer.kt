package service.adapters.demanding.kafka.article

import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import service.application.article.ToImportArticles

@Component
class ArticleConsumer(private val articleImporter: ToImportArticles) {

  @KafkaListener(topics = ["article"])
  fun listen(message: ArticleMessage) {
    articleImporter.importArticle(message.toArticle())
  }
}

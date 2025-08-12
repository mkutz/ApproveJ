package service.adapters.demanding.kafka.article

import java.math.BigDecimal
import java.util.UUID
import service.application.article.Article

data class ArticleMessage(
  val id: String,
  val title: String,
  val description: String,
  val imageUrl: String,
  val price: String,
) {

  fun toArticle() =
    Article(
      id = UUID.fromString(id),
      title = title,
      description = description,
      imageUrl = imageUrl,
      price = BigDecimal(price),
    )
}

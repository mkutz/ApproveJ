package service.adapters.demanding.restapi.article

import service.adapters.demanding.restapi.toCents
import service.application.article.Article

data class ArticleDto(
  val id: String,
  val title: String,
  val description: String,
  val imageUrl: String,
  val pricePerUnit: Int,
) {

  constructor(
    article: Article
  ) : this(
    id = article.id.toString(),
    title = article.title,
    description = article.description,
    imageUrl = article.imageUrl,
    pricePerUnit = article.price.toCents(),
  )
}

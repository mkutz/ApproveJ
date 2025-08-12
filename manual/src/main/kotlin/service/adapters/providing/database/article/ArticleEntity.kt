package service.adapters.providing.database.article

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*
import service.application.article.Article

@Entity
@Table(name = "article")
data class ArticleEntity(
  @Id val id: UUID,
  val title: String,
  val description: String,
  val imageUrl: String,
  val pricePerUnit: BigDecimal,
) {
  constructor(
    article: Article
  ) : this(
    id = article.id,
    title = article.title,
    description = article.description,
    imageUrl = article.imageUrl,
    pricePerUnit = article.price,
  )

  fun toArticle() =
    Article(
      id = id,
      title = title,
      description = description,
      imageUrl = imageUrl,
      price = pricePerUnit,
    )
}

package service.application.article

import java.math.BigDecimal
import java.util.*
import java.util.UUID.randomUUID

class ArticleBuilder private constructor() {
  var id: UUID = randomUUID()
  var title: String = "Some title"
  var description: String = "Some description"
  var imageUrl: String = "https://approvej.org/images/${randomUUID()}"
  var price: BigDecimal = BigDecimal("2.99")

  companion object {
    fun anArticle(): ArticleBuilder = ArticleBuilder()
  }

  fun id(id: UUID) = apply { this.id = id }

  fun title(title: String) = apply { this.title = title }

  fun description(description: String) = apply { this.description = description }

  fun imageUrl(imageUrl: String) = apply { this.imageUrl = imageUrl }

  fun price(price: BigDecimal) = apply { this.price = price }

  fun price(price: String) = apply { this.price = BigDecimal(price) }

  fun build() =
    Article(id = id, title = title, description = description, imageUrl = imageUrl, price = price)
}

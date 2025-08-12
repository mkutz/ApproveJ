package service.application.shoppingcart

import java.math.BigDecimal
import java.util.*
import service.application.article.Article

data class Item(
  val id: UUID,
  val articleId: UUID,
  val title: String,
  val imageUrl: String,
  val quantity: Int,
  val pricePerPiece: BigDecimal,
) {
  val priceTotal: BigDecimal = pricePerPiece.multiply(quantity.toBigDecimal())

  constructor(
    article: Article,
    quantity: Int,
  ) : this(
    id = UUID.randomUUID(),
    articleId = article.id,
    title = article.title,
    imageUrl = article.imageUrl,
    quantity = quantity,
    pricePerPiece = article.price,
  )
}

package service.adapters.providing.database.shoppingcart

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.*
import service.application.shoppingcart.Item

@Entity
@Table(name = "item")
data class ItemEntity(
  @Id val id: UUID,
  val articleId: UUID,
  val title: String,
  val imageUrl: String,
  val quantity: Int,
  val pricePerPiece: BigDecimal,
  val shoppingCartId: UUID,
) {

  constructor(
    item: Item
  ) : this(
    id = item.id,
    articleId = item.articleId,
    title = item.title,
    imageUrl = item.imageUrl,
    quantity = item.quantity,
    pricePerPiece = item.pricePerPiece,
    shoppingCartId = item.id,
  )

  fun toItem() =
    Item(
      id = id,
      articleId = articleId,
      title = title,
      imageUrl = imageUrl,
      quantity = quantity,
      pricePerPiece = pricePerPiece,
    )
}

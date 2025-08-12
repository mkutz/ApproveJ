package service.adapters.demanding.restapi.shoppingcart

import service.adapters.demanding.restapi.toCents
import service.application.shoppingcart.Item

data class ItemDto(
  val id: String,
  val articleId: String,
  val title: String,
  val imageUrl: String,
  val quantity: Int,
  val pricePerPiece: Int,
  val priceTotal: Int,
) {
  constructor(
    item: Item
  ) : this(
    id = item.id.toString(),
    articleId = item.articleId.toString(),
    title = item.title,
    imageUrl = item.imageUrl,
    quantity = item.quantity,
    pricePerPiece = item.pricePerPiece.toCents(),
    priceTotal = item.priceTotal.toCents(),
  )
}

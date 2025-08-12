package service.application.shoppingcart

import java.math.BigDecimal
import java.util.*

data class ShoppingCart(val id: UUID, val items: List<Item>) {

  val value: BigDecimal = items.sumOf { item -> item.priceTotal }
}

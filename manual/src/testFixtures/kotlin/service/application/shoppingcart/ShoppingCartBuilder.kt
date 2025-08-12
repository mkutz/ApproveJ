package service.application.shoppingcart

import java.util.*

class ShoppingCartBuilder private constructor() {
  var id = UUID.randomUUID()
  var items = mutableListOf<Item>()

  companion object {
    fun aShoppingCart() = ShoppingCartBuilder()
  }

  fun id(id: UUID) = apply { this.id = id }

  fun items(vararg items: Item) = apply { this.items.addAll(items) }

  fun build() = ShoppingCart(id, items)
}

package service.adapters.demanding.restapi.shoppingcart

import service.adapters.demanding.restapi.toCents
import service.application.shoppingcart.ShoppingCart

data class ShoppingCartDto(val id: String, val items: List<ItemDto>, val value: Int) {

  constructor(
    shoppingCart: ShoppingCart
  ) : this(
    id = shoppingCart.id.toString(),
    items = shoppingCart.items.map { ItemDto(it) },
    value = shoppingCart.value.toCents(),
  )
}

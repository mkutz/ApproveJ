package service.application.shoppingcart

import java.util.*

interface ToStoreShoppingCarts {

  fun getShoppingCart(shoppingCartId: UUID): ShoppingCart?

  fun storeShoppingCart(shoppingCart: ShoppingCart): ShoppingCart

  fun addItem(shoppingCartId: UUID, item: Item): Result<ShoppingCart>
}

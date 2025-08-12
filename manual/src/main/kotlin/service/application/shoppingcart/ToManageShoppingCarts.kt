package service.application.shoppingcart

import java.util.*

interface ToManageShoppingCarts {

  fun createShoppingCart(): ShoppingCart

  fun getShoppingCart(id: UUID): ShoppingCart?

  fun updateItem(shoppingCartId: UUID, articleId: UUID, quantity: Int): Result<ShoppingCart>
}

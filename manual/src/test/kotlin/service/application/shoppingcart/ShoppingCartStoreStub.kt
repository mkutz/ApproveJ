package service.application.shoppingcart

import java.util.*

class ShoppingCartStoreStub(private val data: MutableMap<UUID, ShoppingCart> = mutableMapOf()) :
  ToStoreShoppingCarts {

  override fun getShoppingCart(shoppingCartId: UUID) = data[shoppingCartId]

  override fun storeShoppingCart(shoppingCart: ShoppingCart): ShoppingCart {
    data[shoppingCart.id] = shoppingCart
    return data[shoppingCart.id]!!
  }

  override fun addItem(shoppingCartId: UUID, item: Item): Result<ShoppingCart> {
    val shoppingCart = data[shoppingCartId]!!
    data[shoppingCartId] = shoppingCart.copy(items = shoppingCart.items + item)
    return Result.success(data[shoppingCartId]!!)
  }
}

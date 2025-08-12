package service.adapters.providing.database.shoppingcart

import jakarta.transaction.Transactional
import java.util.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import service.application.shoppingcart.Item
import service.application.shoppingcart.ShoppingCart
import service.application.shoppingcart.ShoppingCartNotFoundException
import service.application.shoppingcart.ToStoreShoppingCarts

@Component
class ShoppingCartStore(private val shoppingCartRepository: ShoppingCartRepository) :
  ToStoreShoppingCarts {

  override fun getShoppingCart(shoppingCartId: UUID): ShoppingCart? =
    shoppingCartRepository.findByIdOrNull(shoppingCartId)?.toShoppingCart()

  override fun storeShoppingCart(shoppingCart: ShoppingCart): ShoppingCart {
    shoppingCartRepository.save(ShoppingCartEntity(shoppingCart))
    return shoppingCart
  }

  @Transactional
  override fun addItem(shoppingCartId: UUID, item: Item): Result<ShoppingCart> {
    val shoppingCartEntity = shoppingCartRepository.findByIdOrNull(shoppingCartId)
    return when {
      shoppingCartEntity == null -> failure(ShoppingCartNotFoundException(shoppingCartId))
      else ->
        success(
          shoppingCartRepository
            .save(shoppingCartEntity.also { it.items.add(ItemEntity(item)) })
            .toShoppingCart()
        )
    }
  }
}

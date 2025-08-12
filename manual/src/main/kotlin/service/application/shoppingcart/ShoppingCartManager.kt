package service.application.shoppingcart

import java.util.*
import java.util.UUID.randomUUID
import kotlin.Result.Companion.failure
import org.springframework.stereotype.Service
import service.application.article.ToStoreArticles

@Service
class ShoppingCartManager(
  private val shoppingCartStore: ToStoreShoppingCarts,
  private val articleStore: ToStoreArticles,
) : ToManageShoppingCarts {

  override fun createShoppingCart(): ShoppingCart =
    shoppingCartStore.storeShoppingCart(ShoppingCart(randomUUID(), emptyList()))

  override fun getShoppingCart(id: UUID): ShoppingCart? = shoppingCartStore.getShoppingCart(id)

  override fun updateItem(
    shoppingCartId: UUID,
    articleId: UUID,
    quantity: Int,
  ): Result<ShoppingCart> {
    val article = articleStore.getArticleById(articleId)
    val shoppingCart = shoppingCartStore.getShoppingCart(shoppingCartId)
    return when {
      article == null -> failure(UnknownArticleException(articleId))
      shoppingCart == null -> failure(ShoppingCartNotFoundException(shoppingCartId))
      else -> shoppingCartStore.addItem(shoppingCartId, Item(article, quantity))
    }
  }
}

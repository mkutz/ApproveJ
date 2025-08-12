package service.application.shoppingcart

import java.util.UUID.randomUUID
import org.approvej.ApprovalBuilder.approve
import org.approvej.ApprovalError
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import service.application.article.ArticleBuilder.Companion.anArticle
import service.application.article.ArticleStoreStub
import service.application.shoppingcart.ShoppingCartBuilder.Companion.aShoppingCart

class ShoppingCartManagerTest {

  private val shoppingCartStore = ShoppingCartStoreStub()
  private val articleStore = ArticleStoreStub()

  private val shoppingCartManager = ShoppingCartManager(shoppingCartStore, articleStore)

  @Test
  fun createShoppingCart() {
    val createdShoppingCart = shoppingCartManager.createShoppingCart()

    assertThat(createdShoppingCart.id).isNotNull()
    assertThat(createdShoppingCart.items).isEmpty()
    assertThat(createdShoppingCart.value).isZero()
    assertThat(shoppingCartStore.getShoppingCart(createdShoppingCart.id))
      .isEqualTo(createdShoppingCart)

    assertThatExceptionOfType(ApprovalError::class.java).isThrownBy {
      approve(createdShoppingCart).byFile()
    }
  }

  @Test
  fun getShoppingCart() {
    val storedShoppingCart = shoppingCartStore.storeShoppingCart(aShoppingCart().build())

    val gottenShoppingCart = shoppingCartManager.getShoppingCart(storedShoppingCart.id)

    assertThat(gottenShoppingCart).isEqualTo(storedShoppingCart)
  }

  @Test
  fun updateItem() {
    val storedShoppingCartId = shoppingCartStore.storeShoppingCart(aShoppingCart().build()).id
    val article = articleStore.storeArticle(anArticle().build())

    val updatedShoppingCart = shoppingCartManager.updateItem(storedShoppingCartId, article.id, 1)

    assertThat(updatedShoppingCart.isSuccess).isTrue()
    assertThat(shoppingCartStore.getShoppingCart(storedShoppingCartId)!!.items.map { it.articleId })
      .isEqualTo(listOf(article.id))
  }

  @Test
  fun `updateItem shopping cart unknown`() {
    val unknownShoppingCartId = randomUUID()
    val article = articleStore.storeArticle(anArticle().build())

    val updateShoppingCartResult =
      shoppingCartManager.updateItem(unknownShoppingCartId, article.id, 1)

    assertThat(updateShoppingCartResult.isFailure).isTrue()
    assertThat(updateShoppingCartResult.exceptionOrNull())
      .isInstanceOf(ShoppingCartNotFoundException::class.java)
      .hasMessage("Shopping cart $unknownShoppingCartId not found")
  }

  @Test
  fun `updateItem article unknown`() {
    val storedShoppingCartId = shoppingCartStore.storeShoppingCart(aShoppingCart().build()).id
    val unknownArticleId = randomUUID()

    val updateShoppingCartResult =
      shoppingCartManager.updateItem(storedShoppingCartId, unknownArticleId, 1)

    assertThat(updateShoppingCartResult.isFailure).isTrue()
    assertThat(updateShoppingCartResult.exceptionOrNull())
      .isInstanceOf(UnknownArticleException::class.java)
      .hasMessage("Unknown article $unknownArticleId")
  }
}

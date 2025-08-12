package service.application.shoppingcart

import java.math.BigDecimal
import java.util.UUID.randomUUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import service.application.article.ArticleBuilder.Companion.anArticle
import service.application.shoppingcart.ItemBuilder.Companion.anItem

class ShoppingCartTest {

  @Test
  fun `value multiple items`() {
    // tag::simple_assertion[]
    val item1 = anItem().article(anArticle().price("2.5").build()).quantity(1).build()
    val item2 = anItem().article(anArticle().price("7.5").build()).quantity(1).build()
    val shoppingCart = ShoppingCart(randomUUID(), listOf(item1, item2))

    assertThat(shoppingCart.value).isEqualTo(item1.priceTotal.plus(item2.priceTotal))
    // end::simple_assertion[]
  }

  @Test
  fun `value one item`() {
    val item = anItem().article(anArticle().price("2.5").build()).quantity(5).build()
    val shoppingCart = ShoppingCart(randomUUID(), listOf(item))

    assertThat(shoppingCart.value).isEqualTo(item.priceTotal)
  }

  @Test
  fun `value no items`() {
    val shoppingCart = ShoppingCart(randomUUID(), emptyList())

    assertThat(shoppingCart.value).isEqualTo(BigDecimal.ZERO)
  }
}

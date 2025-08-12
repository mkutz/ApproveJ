package service.adapters.providing.database.shoppingcart

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.*
import service.application.shoppingcart.ShoppingCart

@Entity
@Table(name = "shopping_cart")
data class ShoppingCartEntity(
  @Id val id: UUID,
  @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinColumn(name = "shoppingCartId")
  val items: MutableList<ItemEntity>,
) {

  constructor(
    shoppingCart: ShoppingCart
  ) : this(id = shoppingCart.id, items = shoppingCart.items.map { ItemEntity(it) }.toMutableList())

  fun toShoppingCart() = ShoppingCart(id = id, items = items.map { it.toItem() })
}

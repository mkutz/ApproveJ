package service.application.shoppingcart

class ShoppingCartNotFoundException(val shoppingCartId: java.util.UUID) :
  RuntimeException("Shopping cart $shoppingCartId not found")

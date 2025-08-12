package service.adapters.providing.database.shoppingcart

import java.util.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository interface ShoppingCartRepository : CrudRepository<ShoppingCartEntity, UUID>

package service.application.article

import java.math.BigDecimal
import java.util.*

data class Article(
  val id: UUID,
  val title: String,
  val description: String,
  val imageUrl: String,
  val price: BigDecimal,
)

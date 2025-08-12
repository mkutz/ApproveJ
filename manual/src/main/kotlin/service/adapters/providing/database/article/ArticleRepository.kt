package service.adapters.providing.database.article

import java.util.*
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleRepository : CrudRepository<ArticleEntity, UUID> {

  fun findAllByTitleContainsIgnoreCase(query: String): List<ArticleEntity>
}

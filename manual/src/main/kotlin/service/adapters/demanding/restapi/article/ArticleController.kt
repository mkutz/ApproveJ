package service.adapters.demanding.restapi.article

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import service.application.article.ToStoreArticles

@RestController
class ArticleController(private val articleQuery: ToStoreArticles) {

  @GetMapping
  fun findArticles(@RequestParam(name = "q") query: String): List<ArticleDto> {
    return articleQuery.findArticles(query).map { ArticleDto(it) }
  }
}

package service.adapters.demanding.restapi.article

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import service.application.article.ToQueryArticles

@RestController
class ArticleController(private val articleQuery: ToQueryArticles) {

  @GetMapping
  fun findArticles(@RequestParam(name = "q") query: String): List<ArticleDto> {
    return articleQuery.queryArticles(query).map { ArticleDto(it) }
  }
}
